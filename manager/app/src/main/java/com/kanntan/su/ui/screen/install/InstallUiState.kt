@file:Suppress("unused")

package com.kanntan.su.ui.screen.install

import me.weishu.kernelsu.ui.util.LkmSelection

data class InstallUiState(
    val installMethod: InstallMethod? = null,
    val lkmSelection: LkmSelection = LkmSelection.KmiNone,
    val partitionSelectionIndex: Int = 0,
    val displayPartitions: List<String> = emptyList(),
    val currentKmi: String = "",
    val slotSuffix: String = "",
    val installMethodOptions: List<InstallMethodOption> = emptyList(),
    val canSelectPartition: Boolean = false,
    val advancedOptionsShown: Boolean = false,
    val allowShell: Boolean = false,
    val enableAdb: Boolean = false
)

sealed class InstallMethod {
    data class SelectFile(val uri: android.net.Uri, val summary: String) : InstallMethod()
    data object DirectInstall : InstallMethod()
    data object DirectInstallToInactiveSlot : InstallMethod()
}

data class InstallMethodOption(
    val label: Int,
    val summary: String
)

data class InstallScreenActions(
    val onBack: () -> Unit = {},
    val onSelectMethod: (InstallMethod) -> Unit = {},
    val onSelectBootImage: () -> Unit = {},
    val onUploadLkm: () -> Unit = {},
    val onClearLkm: () -> Unit = {},
    val onSelectPartition: (Int) -> Unit = {},
    val onNext: () -> Unit = {},
    val onAdvancedOptionsClicked: () -> Unit = {},
    val onSelectAllowShell: (Boolean) -> Unit = {},
    val onSelectEnableAdb: (Boolean) -> Unit = {}
)

fun isKoFile(context: android.content.Context, uri: android.net.Uri): Boolean {
    return context.contentResolver.getType(uri)?.contains("ko") == true ||
           uri.lastPathSegment?.endsWith(".ko") == true
}
