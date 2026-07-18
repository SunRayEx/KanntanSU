package com.kanntan.su.ui.screen.module

import android.net.Uri

/**
 * Module Screen UI State for KanntanSU
 */
data class ModuleUiState(
    val moduleList: List<ModuleItem> = emptyList(),
    val searchText: String = "",
    val searchResults: List<ModuleItem> = emptyList(),
    val isRefreshing: Boolean = false,
    val magiskInstalled: Boolean = false,
    val installButtonVisible: Boolean = true,
    val sortActionFirst: Boolean = false,
    val sortEnabledFirst: Boolean = true,
    val updateInfo: Map<String, ModuleUpdateInfo> = emptyMap(),
    val confirmDialogState: ModuleConfirmDialogState? = null,
    val effect: ModuleEffect? = null
)

data class ModuleItem(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val enabled: Boolean = true,
    val remove: Boolean = false,
    val update: Boolean = false,
    val hasWebUi: Boolean = false,
    val hasActionScript: Boolean = false,
    val metamodule: Boolean = false,
    val zygisk: Boolean = false
)

data class ModuleUpdateInfo(
    val versionCode: Int = 0,
    val downloadUrl: String = "",
    val changelog: String = ""
)

data class ModuleConfirmDialogState(
    val title: String,
    val content: String? = null,
    val markdown: Boolean = false,
    val html: Boolean = false,
    val confirm: String? = null,
    val dismiss: String? = null,
    val request: ModuleConfirmRequest? = null
)

sealed class ModuleConfirmRequest {
    data class Uninstall(val module: ModuleItem) : ModuleConfirmRequest()
    data class Update(val module: ModuleItem, val downloadUrl: String, val fileName: String) : ModuleConfirmRequest()
}

sealed class ModuleEffect {
    data class Toast(val message: String) : ModuleEffect()
    data class SnackBar(val message: String) : ModuleEffect()
}

data class ModuleActions(
    val onRefresh: () -> Unit = {},
    val onSearchStatusChange: (SearchStatus) -> Unit = {},
    val onSearchTextChange: (String) -> Unit = {},
    val onClearSearch: () -> Unit = {},
    val onRequestUpdateConfirmation: (ModuleItem, ModuleUpdateInfo) -> Unit = { _, _ -> },
    val onRequestUninstallConfirmation: (ModuleItem) -> Unit = {},
    val onDismissConfirmRequest: () -> Unit = {},
    val onConsumeEffect: () -> Unit = {},
    val onConfirmUpdate: (ModuleConfirmRequest.Update) -> Unit = {},
    val onOpenRepo: () -> Unit = {},
    val onToggleSortActionFirst: () -> Unit = {},
    val onToggleSortEnabledFirst: () -> Unit = {},
    val onOpenWebUi: (ModuleItem) -> Unit = {},
    val onToggleModule: (ModuleItem) -> Unit = {},
    val onUninstallModule: (ModuleItem) -> Unit = {},
    val onUndoUninstallModule: (ModuleItem) -> Unit = {},
    val onOpenFlash: (List<Uri>) -> Unit = {},
    val onExecuteModuleAction: (ModuleItem) -> Unit = {}
)

enum class SearchStatus { IDLE, ACTIVE }

enum class ShortcutType { Action, WebUI }

data class ModuleShortcutState(
    val moduleId: String = "",
    val name: String = "",
    val iconUri: String? = null,
    val defaultShortcutIconUri: String = "",
    val hasExistingShortcut: Boolean = false,
    val previewIcon: android.graphics.Bitmap? = null
)

class RememberedModuleShortcutState {
    private val _moduleId = androidx.compose.runtime.mutableStateOf("")
    private val _name = androidx.compose.runtime.mutableStateOf("")
    private val _iconUri = androidx.compose.runtime.mutableStateOf<String?>(null)
    private val _defaultShortcutIconUri = androidx.compose.runtime.mutableStateOf("")
    private val _hasExistingShortcut = androidx.compose.runtime.mutableStateOf(false)
    private val _previewIcon = androidx.compose.runtime.mutableStateOf<android.graphics.Bitmap?>(null)
    
    var moduleId: String
        get() = _moduleId.value
        set(value) { _moduleId.value = value }
    
    var name: String
        get() = _name.value
        set(value) { _name.value = value }
    
    var iconUri: String?
        get() = _iconUri.value
        set(value) { _iconUri.value = value }
    
    var defaultShortcutIconUri: String
        get() = _defaultShortcutIconUri.value
        set(value) { _defaultShortcutIconUri.value = value }
    
    var hasExistingShortcut: Boolean
        get() = _hasExistingShortcut.value
        set(value) { _hasExistingShortcut.value = value }
    
    var previewIcon: android.graphics.Bitmap?
        get() = _previewIcon.value
        set(value) { _previewIcon.value = value }
    
    fun bindModule(module: ModuleItem) {
        moduleId = module.id
        name = module.name
    }
    
    fun selectType(type: ShortcutType) {}
    fun updateIconUri(uri: String?) {}
    fun updateName(newName: String) { name = newName }
    fun resetIconToDefault() {}
    fun deleteShortcut(context: android.content.Context) {}
    fun createShortcut(context: android.content.Context) {}
}

fun rememberModuleShortcutState() = RememberedModuleShortcutState()