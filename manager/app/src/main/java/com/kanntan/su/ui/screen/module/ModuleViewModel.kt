package com.kanntan.su.ui.screen.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Module ViewModel - 管理模块列表状态
 */
class ModuleViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(ModuleUiState())
    val uiState: StateFlow<ModuleUiState> = _uiState.asStateFlow()
    
    init {
        refresh()
    }
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            delay(500)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
    
    fun toggleModule(module: ModuleItem) {
        val updatedList = _uiState.value.moduleList.map {
            if (it.id == module.id) it.copy(enabled = !it.enabled) else it
        }
        _uiState.update { it.copy(moduleList = updatedList) }
    }
    
    fun uninstallModule(module: ModuleItem) {
        val updatedList = _uiState.value.moduleList.filter { it.id != module.id }
        _uiState.update { it.copy(moduleList = updatedList) }
    }
}