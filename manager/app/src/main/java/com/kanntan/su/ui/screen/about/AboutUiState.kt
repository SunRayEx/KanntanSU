package com.kanntan.su.ui.screen.about

/**
 * About Screen UI State for KanntanSU
 */
data class AboutUiState(
    val title: String = "",
    val appName: String = "",
    val versionName: String = "",
    val links: List<LinkInfo> = emptyList()
)

data class LinkInfo(
    val fullText: String,
    val url: String
)

data class AboutScreenActions(
    val onBack: () -> Unit = {},
    val onOpenLink: (String) -> Unit = {}
)