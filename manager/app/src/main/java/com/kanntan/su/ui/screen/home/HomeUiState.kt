package com.kanntan.su.ui.screen.home

/**
 * Home Screen UI State for KanntanSU
 * 
 * @property systemInfo Real-time system information
 * @property ksuVersion KernelSU module version (null if not installed)
 * @property isRootAvailable Whether root is available
 * @property isSafeMode Whether in safe mode
 * @property isLateLoadMode Whether module loads late
 * @property isManager Whether this is the manager
 * @property menuState Menu visibility state
 * @property selectedMenuItem Currently selected menu item
 */
data class HomeUiState(
    val systemInfo: SystemInfo = SystemInfo(
        kernelVersion = "",
        managerVersion = "",
        fingerprint = "",
        selinuxStatus = "",
        seccompStatus = -1
    ),
    val ksuVersion: Int? = null,
    val isRootAvailable: Boolean = false,
    val isSafeMode: Boolean = false,
    val isLateLoadMode: Boolean = false,
    val isManager: Boolean = false,
    val menuState: MenuState = MenuState.HIDDEN,
    val selectedMenuItem: MenuItemType? = null,
    val superuserCount: Int = 0,
    val moduleCount: Int = 0,
    val navigateTo: Screen? = null
) {
    enum class MenuState { HIDDEN, EXPANDED, SELECTED }
    
    enum class MenuItemType { MODULES, APPLICATION, SETTING }
    
    enum class Screen { MODULES, APPLICATION, SETTING, INSTALL }
    
    /**
     * Get KSU status message for display
     */
    fun getKSUStatusMessage(): String {
        val status = determineKSUStatus(
            ksuVersion = ksuVersion,
            isRootAvailable = isRootAvailable,
            isManager = isManager,
            requiresNewKernel = false
        )
        return when (status) {
            // Root 已可用，KernelSU 正常工作
            SystemInfo.KSUStatus.READY -> "Ready · 已获得 Root"
            // 没有检测到内核模块：尚未刷入或刷入后未重启
            SystemInfo.KSUStatus.NOT_READY -> "Not Ready · 未检测到内核模块"
            SystemInfo.KSUStatus.UNSUPPORT -> "Unsupport · 需要更新内核"
            // 内核已加载且已认本应用为管理器，只是尚未提权到 Root：点击顶部即可提权
            // （SELinux 宽容时走 Magica 提权，否则跳转刷入）
            SystemInfo.KSUStatus.CAN_PRIVILEGE -> "Can Privilege · 可提权，点击顶部获取 Root"
            SystemInfo.KSUStatus.UNKNOWN -> "Unknown · 未知"
        }
    }
}

/**
 * Home screen user action callbacks
 */
data class HomeActions(
    val onInstallClick: () -> Unit = {},
    val onModulesClick: () -> Unit = {},
    val onApplicationClick: () -> Unit = {},
    val onSettingClick: () -> Unit = {},
    val onMenuToggle: () -> Unit = {},
    val onMenuDismiss: () -> Unit = {},
    val onJailbreakClick: () -> Unit = {}
)