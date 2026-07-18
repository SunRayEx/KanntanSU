package com.kanntan.su.ui.screen.home

import android.content.Context
import android.os.Build
import android.system.Os
import androidx.compose.runtime.Immutable
import androidx.core.content.pm.PackageInfoCompat

/**
 * System Information Data Class for KanntanSU
 * 
 * All values are real-time, fetched dynamically from Android system
 * and KernelSU daemon (ksud). No hardcoded values.
 * 
 * @property kernelVersion Linux kernel version from Os.uname().release
 * @property managerVersion App version name and code from package manager
 * @property fingerprint System fingerprint from Build.FINGERPRINT
 * @property selinuxStatus SELinux status: "Enforcing", "Permissive", "Disabled", "Unknown"
 * @property seccompStatus Seccomp status: -1=unsupported, 0=disabled, 1=strict, 2=filter
 */
@Immutable
data class SystemInfo(
    val kernelVersion: String,
    val managerVersion: String,
    val fingerprint: String,
    val selinuxStatus: String,
    val seccompStatus: Int,
    val ksuStatus: KSUStatus = KSUStatus.UNKNOWN
) {
    /**
     * KSU Status enum for display messages
     */
    enum class KSUStatus {
        READY,
        NOT_READY,
        UNSUPPORT,
        CAN_PRIVILEGE,
        UNKNOWN
    }
}

/**
 * Manager Version from package manager
 */
@Immutable
data class ManagerVersion(
    val versionName: String,
    val versionCode: Long
)

/**
 * Get Manager Version from package manager
 * 
 * Real-time fetch from Context.packageManager
 */
fun getManagerVersion(context: Context): ManagerVersion {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)!!
    val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
    return ManagerVersion(
        versionName = packageInfo.versionName!!,
        versionCode = versionCode
    )
}

/**
 * Get system fingerprint
 * 
 * Real-time from Build.FINGERPRINT
 */
fun getSystemFingerprint(): String = Build.FINGERPRINT

/**
 * Get kernel version string
 * 
 * Real-time from Os.uname().release
 */
fun getKernelVersionString(): String = Os.uname().release

/**
 * Get SELinux status from system
 * 
 * Executes 'getenforce' shell command
 */
fun getSELinuxStatus(): String {
    return try {
        val shell = com.topjohnwu.superuser.Shell.Builder.create().build("sh")
        val stdoutList = ArrayList<String>()
        val stderrList = ArrayList<String>()
        val result = shell.use {
            it.newJob().add("getenforce").to(stdoutList, stderrList).exec()
        }
        val stdout = stdoutList.joinToString("\n").trim()
        
        if (result.isSuccess) {
            when (stdout) {
                "Enforcing", "Permissive", "Disabled" -> stdout
                else -> "Unknown"
            }
        } else {
            "Unknown"
        }
    } catch (e: Exception) {
        "Unknown"
    }
}

/**
 * Get seccomp status via prctl
 * 
 * PR_GET_SECCOMP = 21
 */
fun getSeccompStatus(): Int {
    return try {
        Os.prctl(21, 0, 0, 0, 0)
    } catch (e: Exception) {
        -1
    }
}

/**
 * Determine KSU status based on kernel module state
 */
fun determineKSUStatus(
    ksuVersion: Int?,
    isRootAvailable: Boolean,
    isManager: Boolean,
    requiresNewKernel: Boolean
): SystemInfo.KSUStatus {
    return when {
        ksuVersion == null || !isRootAvailable -> {
            if (isManager && requiresNewKernel) {
                SystemInfo.KSUStatus.UNSUPPORT
            } else if (ksuVersion != null && !isRootAvailable) {
                SystemInfo.KSUStatus.CAN_PRIVILEGE
            } else {
                SystemInfo.KSUStatus.NOT_READY
            }
        }
        isRootAvailable && !requiresNewKernel -> SystemInfo.KSUStatus.READY
        else -> SystemInfo.KSUStatus.NOT_READY
    }
}