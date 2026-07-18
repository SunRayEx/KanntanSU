package com.kanntan.su.ui.screen.flash

import me.weishu.kernelsu.ui.screen.flash.FlashIt
import me.weishu.kernelsu.ui.screen.flash.FlashingStatus

data class FlashUiState(
    val text: String = "",
    val showRebootAction: Boolean = false,
    val flashingStatus: FlashingStatus = FlashingStatus.FLASHING,
    val showJailbreakWarning: Boolean = false
)

data class FlashScreenActions(
    val onBack: () -> Unit = {},
    val onSaveLog: () -> Unit = {},
    val onReboot: () -> Unit = {},
    val onConfirmJailbreakWarning: () -> Unit = {},
    val onDismissJailbreakWarning: () -> Unit = {}
)
