package com.kanntan.su

/**
 * Natives - JNI bindings to KernelSU kernel module
 * 
 * This file is kept for reference only. The actual JNI bindings
 * are provided by me.weishu.kernelsu.Natives which uses libkernelsu.so.
 * 
 * In a production KanntanSU fork, you would either:
 * 1. Use me.weishu.kernelsu.Natives directly (recommended for compatibility)
 * 2. Or rebuild libkernelsu.so with JNI function names matching com.kanntan.su
 * 
 * For the UI implementation, import and use:
 * import me.weishu.kernelsu.Natives
 * 
 * @see me.weishu.kernelsu.Natives
 */
@Suppress("EMPTY_BLOCK_BLOCK_BODY")
object Natives {
    // This object is deprecated - use me.weishu.kernelsu.Natives instead
    // All functionality is provided by the original Natives class
}
