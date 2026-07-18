package com.kanntan.su.ui.screen.executemoduleaction

/**
 * Execute Module Action Screen UI State for KanntanSU
 */
data class ExecuteModuleActionUiState(
    val text: String = ""
)

data class ExecuteModuleActionScreenActions(
    val onBack: () -> Unit = {},
    val onSaveLog: () -> Unit = {}
)