package com.kanntan.su.ui.screen.install

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.weishu.kernelsu.ui.util.LkmSelection

/**
 * Install ViewModel - 管理安装状态
 */
class InstallViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(InstallUiState())
    val uiState: StateFlow<InstallUiState> = _uiState.asStateFlow()
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy() }
        }
    }
    
    fun startInstall() {
        viewModelScope.launch {
            _uiState.update { it.copy(installMethod = InstallMethod.DirectInstall) }
        }
    }
    
    fun selectMethod(method: InstallMethod) {
        _uiState.update { it.copy(installMethod = method) }
    }
    
    fun selectLkm(lkm: LkmSelection) {
        _uiState.update { it.copy(lkmSelection = lkm) }
    }
}