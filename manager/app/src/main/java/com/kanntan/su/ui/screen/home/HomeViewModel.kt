package com.kanntan.su.ui.screen.home

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Home ViewModel - fetches real-time data from system and ksud
 */
class HomeViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private fun buildInitialState() = HomeUiState(
        systemInfo = SystemInfo(
            kernelVersion = "Loading...",
            managerVersion = "Loading...",
            fingerprint = "Loading...",
            selinuxStatus = "Loading...",
            seccompStatus = -1
        )
    )
    
    /**
     * Refresh all data
     */
    fun refresh() {
        viewModelScope.launch {
            val newState = withContext(Dispatchers.IO) { buildFullState() }
            _uiState.update { newState }
        }
    }
    
    fun toggleMenu() {
        _uiState.update { current ->
            val newMenuState = when (current.menuState) {
                HomeUiState.MenuState.HIDDEN -> HomeUiState.MenuState.EXPANDED
                else -> HomeUiState.MenuState.HIDDEN
            }
            current.copy(menuState = newMenuState, selectedMenuItem = null)
        }
    }
    
    fun selectMenuItem(item: HomeUiState.MenuItemType) {
        _uiState.update { it.copy(menuState = HomeUiState.MenuState.SELECTED, selectedMenuItem = item) }
    }
    
    fun dismissMenu() {
        _uiState.update { it.copy(menuState = HomeUiState.MenuState.HIDDEN, selectedMenuItem = null) }
    }
    
    fun navigateTo(screen: HomeUiState.Screen) {
        android.util.Log.d("HomeVM", "navigateTo called: $screen")
        _uiState.update { it.copy(navigateTo = screen, menuState = HomeUiState.MenuState.HIDDEN) }
    }
    
    fun consumeNavigation() {
        android.util.Log.d("HomeVM", "consumeNavigation called")
        _uiState.update { it.copy(navigateTo = null) }
    }
    
    /**
     * Build complete state with real-time data
     */
    private fun buildFullState(): HomeUiState {
        val kernelVersion = try {
            val uname = Runtime.getRuntime().exec("uname -r")
            uname.inputStream.bufferedReader().readText().trim()
        } catch (e: Exception) { "Unknown" }
        
        val managerVersion = try {
            val context = android.app.Application().applicationContext
            val pkg = context.packageManager.getPackageInfo(context.packageName, 0)
            "${pkg.versionName}"
        } catch (e: Exception) { "Unknown" }
        
        val fingerprint = Build.FINGERPRINT
        val selinuxStatus = "Unknown"
        val seccompStatus = -1
        
        return HomeUiState(
            systemInfo = SystemInfo(
                kernelVersion = kernelVersion,
                managerVersion = managerVersion,
                fingerprint = fingerprint,
                selinuxStatus = selinuxStatus,
                seccompStatus = seccompStatus,
                ksuStatus = SystemInfo.KSUStatus.UNKNOWN
            ),
            ksuVersion = null,
            isRootAvailable = false,
            isSafeMode = false,
            isLateLoadMode = false,
            isManager = false,
            superuserCount = 0,
            moduleCount = 0
        )
    }
    
    private fun getModuleCount(): Int {
        return 0 // Placeholder
    }
    
    private fun getSuperuserCount(): Int {
        return 0 // Placeholder
    }
}

/**
 * Preview helper
 */
fun previewHomeUiState() = HomeUiState(
    systemInfo = SystemInfo(
        kernelVersion = "5.15.0-android14-0-g1234567",
        managerVersion = "1.0.0",
        fingerprint = "google/raven/raven:14/AP1A.240305.019:user/release-keys",
        selinuxStatus = "Enforcing",
        seccompStatus = 2,
        ksuStatus = SystemInfo.KSUStatus.READY
    ),
    ksuVersion = 32377,
    isRootAvailable = true,
    isManager = true
)