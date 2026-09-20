#!/usr/bin/env python3
"""Re-target which manager APK certificate a prebuilt kernelsu.ko trusts.

Why this exists
---------------
KernelSU's kernel module recognizes "the manager app" via
`is_manager_apk()` (kernel/manager/apk_sign.c): it parses the APK Signature
v2 block and accepts the APK only when the *first* certificate

  1. has a DER length equal to the compiled-in KSU_EXPECTED_SIZE, AND
  2. hashes (SHA-256) to the compiled-in KSU_EXPECTED_HASH.

Those two constants are baked into every released kernelsu.ko and match the
official KernelSU manager's signing certificate. A fork that ships its own
manager APK under a different certificate is therefore never recognized as
manager: `ksud boot-patch` still succeeds ("刷入成功") but after flashing the
patched boot and rebooting, the kernel grants root to nobody.

This script re-points an already-built aarch64 module at a different
certificate by

  * overwriting the 64-char expected-hash ASCII string in .rodata, and
  * re-encoding the immediate of the single `cmp wN, #<expected_size>`
    instruction (ARM64 SUBS-immediate, imm12 field at bits [21:10]).

It is a development convenience. The proper long-term fix is to build the
modules from source (kernel/build-all.sh via `ddk`) with
`KSU_EXPECTED_SIZE`/`KSU_EXPECTED_HASH` set to the fork's own release
signing key.

Finding the values for your certificate
---------------------------------------
Sign the manager APK, then:

  # SHA-256 of the DER certificate (this is KSU_EXPECTED_HASH):
  apksigner verify --print-certs app.apk | grep 'SHA-256'

  # DER length of the certificate (this is KSU_EXPECTED_SIZE):
  keytool -exportcert -keystore your.jks -alias youralias \
      -file cert.der && stat -c%s cert.der

NOTE: the certificate must stay stable for the life of the flashed modules
(sign with the same key). Re-signing with a different key requires
re-running this script with the new values.

Usage
-----
  python3 scripts/retarget_manager_trust.py \\
      --dir userspace/ksud/bin/aarch64 \\
      --hash 02930c0c145fa5675f312aed4f922aaeb313098ccc8e01d574dfc01ec278afb9 \\
      --size 744 \\
      --objdump $ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-objdump

Afterwards rebuild ksud so it re-embeds the patched modules:
  cargo ndk -t arm64-v8a -P 26 build --release -p ksud
"""

import argparse
import re
import struct
import subprocess
import sys
from pathlib import Path

# The certificate the upstream prebuilt modules are built to trust.
UPSTREAM_HASH = "c371061b19d8c7d7d6133c6a9bafe198fa944e50c1b31c9d8daa8d7f1fc2d2d6"
UPSTREAM_SIZE = 0x033B


def find_section(data: bytes, name: str):
    e_shoff = struct.unpack_from("<Q", data, 0x28)[0]
    e_shentsize = struct.unpack_from("<H", data, 0x3A)[0]
    e_shnum = struct.unpack_from("<H", data, 0x3C)[0]
    e_shstrndx = struct.unpack_from("<H", data, 0x3E)[0]
    secs = []
    for i in range(e_shnum):
        off = e_shoff + i * e_shentsize
        fields = struct.unpack_from("<IIQQQQ", data, off)
        secs.append(fields)  # sh_name, sh_type, sh_flags, sh_addr, sh_offset, sh_size
    strtab_off = secs[e_shstrndx][4]
    for sh_name, _type, _flags, sh_addr, sh_offset, sh_size in secs:
        end = data.index(b"\0", strtab_off + sh_name)
        if data[strtab_off + sh_name : end].decode() == name:
            return sh_addr, sh_offset, sh_size
    raise LookupError(f"section {name} not found")


def patch_ko(path: Path, new_size: int, new_hash: str, objdump: str) -> None:
    data = bytearray(path.read_bytes())

    # --- expected hash string (64 ASCII hex chars, null terminated) ---
    old = UPSTREAM_HASH.encode()
    new = new_hash.encode()
    if len(new) != 64 or not re.fullmatch(rb"[0-9a-f]{64}", new):
        sys.exit(f"--hash must be 64 lowercase hex chars, got {new_hash!r}")
    idx = data.find(old)
    if idx < 0:
        # Already patched to something else?
        if new in data:
            print(f"  {path.name}: already trusts this certificate, skipped")
            return
        sys.exit(f"  {path.name}: expected-hash string not found (already patched?)")
    data[idx : idx + 64] = new

    # --- expected size immediate ---
    text_addr, text_off, _ = find_section(bytes(data), ".text")
    disasm = subprocess.run(
        [objdump, "-d", str(path)], capture_output=True, text=True
    ).stdout
    hits = []
    for line in disasm.splitlines():
        m = re.match(
            r"^\s*([0-9a-f]+):\s+([0-9a-f]{8})\s+.*#(?:0x)?([0-9a-f]+)\s*$", line
        )
        if m and int(m.group(3), 16) == UPSTREAM_SIZE:
            hits.append((int(m.group(1), 16), int(m.group(2), 16)))
    if len(hits) != 1:
        sys.exit(f"  {path.name}: expected 1 cmp #{UPSTREAM_SIZE:#x}, got {len(hits)}")

    addr, word = hits[0]
    if (word >> 22) != 0b0111000100:
        sys.exit(f"  {path.name}: 0x{word:08x} is not a SUBS-immediate (CMP)")
    if (word >> 10) & 0xFFF != UPSTREAM_SIZE:
        sys.exit(f"  {path.name}: decoded imm12 != {UPSTREAM_SIZE:#x}")
    if not (new_size < 0x1000):
        sys.exit(f"  {path.name}: new size {new_size} does not fit in imm12")

    new_word = (word & ~(0xFFF << 10)) | ((new_size & 0xFFF) << 10)
    file_off = text_off + (addr - text_addr)
    if struct.unpack_from("<I", data, file_off)[0] != word:
        sys.exit(f"  {path.name}: instruction word mismatch at 0x{file_off:x}")
    struct.pack_into("<I", data, file_off, new_word)

    path.write_bytes(data)
    print(
        f"  {path.name}: hash @0x{idx:x}, size imm @0x{file_off:x} "
        f"({UPSTREAM_SIZE} -> {new_size}, 0x{word:08x} -> 0x{new_word:08x})"
    )


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0].strip())
    ap.add_argument("--dir", required=True, type=Path, help="dir with *_kernelsu.ko")
    ap.add_argument("--hash", required=True, help="new KSU_EXPECTED_HASH (64 hex)")
    ap.add_argument("--size", required=True, type=lambda x: int(x, 0), help="new KSU_EXPECTED_SIZE")
    ap.add_argument("--objdump", required=True, help="llvm-objdump (NDK)")
    args = ap.parse_args()

    kos = sorted(args.dir.glob("android*_kernelsu.ko"))
    if not kos:
        sys.exit(f"no *_kernelsu.ko under {args.dir}")
    print(f"Re-targeting {len(kos)} module(s) to trust size={args.size} hash={args.hash}")
    for ko in kos:
        patch_ko(ko, args.size, args.hash, args.objdump)
    print("done. Rebuild ksud to re-embed the patched modules.")


if __name__ == "__main__":
    main()
