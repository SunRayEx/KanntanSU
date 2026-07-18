package com.kanntan.su.ui.screen.settings

/**
 * Settings Screen UI State for KanntanSU
 * 
 * Manages all settings configuration state
 */
data class SettingsUiState(
    val checkUpdate: Boolean = true,
    val checkModuleUpdate: Boolean = true,
    val uiMode: Int = 0, // 0=Miuix, 1=Material
    val suCompatStatus: String = "supported",
    val suCompatMode: Int = 0,
    val kernelUmountStatus: String = "supported",
    val isKernelUmountEnabled: Boolean = false,
    val sulogStatus: String = "supported",
    val isSulogEnabled: Boolean = false,
    val adbRootStatus: String = "supported",
    val isAdbRootEnabled: Boolean = false,
    val isDefaultUmountModules: Boolean = false,
    val defaultUnmountModules: Boolean = false,
    val enableWebDebugging: Boolean = false,
    val autoJailbreak: Boolean = false,
    val isLkmMode: Boolean = false,
    val isLateLoadMode: Boolean = false
)

/**
 * Settings screen user action callbacks
 */
data class SettingsScreenActions(
    val onSetCheckUpdate: (Boolean) -> Unit = {},
    val onSetCheckModuleUpdate: (Boolean) -> Unit = {},
    val onOpenTheme: () -> Unit = {},
    val onSetUiModeIndex: (Int) -> Unit = {},
    val onOpenProfileTemplate: () -> Unit = {},
    val onSetSuCompatMode: (Int) -> Unit = {},
    val onSetKernelUmountEnabled: (Boolean) -> Unit = {},
    val onSetSulogEnabled: (Boolean) -> Unit = {},
    val onSetAdbRootEnabled: (Boolean) -> Unit = {},
    val onSetDefaultUmountModules: (Boolean) -> Unit = {},
    val onSetEnableWebDebugging: (Boolean) -> Unit = {},
    val onSetAutoJailbreak: (Boolean) -> Unit = {},
    val onOpenAbout: () -> Unit = {}
)