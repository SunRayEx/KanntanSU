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
     * Returns: "is Ready", "is Not Ready", "is Unsupport", "is can Privilege"
     */
    fun getKSUStatusMessage(): String {
        val status = determineKSUStatus(
            ksuVersion = ksuVersion,
            isRootAvailable = isRootAvailable,
            isManager = isManager,
            requiresNewKernel = false
        )
        return when (status) {
            SystemInfo.KSUStatus.READY -> "Ready"
            SystemInfo.KSUStatus.NOT_READY -> "Not Ready"
            SystemInfo.KSUStatus.UNSUPPORT -> "Unsupport"
            SystemInfo.KSUStatus.CAN_PRIVILEGE -> "can Privilege"
            SystemInfo.KSUStatus.UNKNOWN -> "Unknown"
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