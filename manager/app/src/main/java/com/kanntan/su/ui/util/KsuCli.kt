package com.kanntan.su.ui.util

import android.net.Uri
import android.util.Log
import me.weishu.kernelsu.ui.util.getAvailablePartitions as upstreamGetAvailablePartitions
import me.weishu.kernelsu.ui.util.getDefaultPartition as upstreamGetDefaultPartition
import me.weishu.kernelsu.ui.util.rootAvailable as upstreamRootAvailable
import me.weishu.kernelsu.ui.util.installBoot as upstreamInstallBoot
import me.weishu.kernelsu.ui.util.LkmSelection
import me.weishu.kernelsu.ui.util.FlashResult as UpstreamFlashResult
import me.weishu.kernelsu.ui.util.reboot as upstreamReboot

private const val TAG = "KanntanSU"

data class FlashResult(
    val isSuccess: Boolean,
    val showReboot: Boolean,
    val output: String = "",
    val error: String = ""
)

fun rootAvailable(): Boolean = upstreamRootAvailable()

suspend fun getCurrentKmi(): String = me.weishu.kernelsu.ui.util.getCurrentKmi()

suspend fun getAvailablePartitions(): List<String> = upstreamGetAvailablePartitions()

suspend fun getDefaultPartition(): String = upstreamGetDefaultPartition()

suspend fun isAbDevice(): Boolean = me.weishu.kernelsu.ui.util.isAbDevice()

suspend fun getSlotSuffix(ota: Boolean): String = me.weishu.kernelsu.ui.util.getSlotSuffix(ota)

fun installBoot(
    bootUri: Uri?,
    lkm: LkmSelection,
    ota: Boolean,
    partition: String?,
    allowShell: Boolean,
    enableAdb: Boolean,
    onStdout: (String) -> Unit,
    onStderr: (String) -> Unit,
): FlashResult {
    val result = upstreamInstallBoot(
        bootUri = bootUri,
        lkm = lkm,
        ota = ota,
        partition = partition,
        allowShell = allowShell,
        enableAdb = enableAdb,
        onStdout = onStdout,
        onStderr = onStderr,
    )
    return FlashResult(
        isSuccess = result.code == 0,
        showReboot = result.showReboot,
        output = "",
        error = result.err
    )
}

fun reboot(reason: String = "") {
    upstreamReboot(reason)
}

fun execKsud(cmd: String, root: Boolean = false): Boolean {
    Log.i(TAG, "Execute ksud: $cmd, root: $root")
    return me.weishu.kernelsu.ui.util.execKsud(cmd, root)
}

fun getFeatureStatus(feature: String): String {
    return when (feature) {
        "su_compat" -> if (me.weishu.kernelsu.Natives.isSuEnabled()) "supported" else "unsupported"
        "kernel_umount" -> if (me.weishu.kernelsu.Natives.isKernelUmountEnabled()) "enabled" else "disabled"
        "sulog" -> "supported"
        else -> "unknown"
    }
}
