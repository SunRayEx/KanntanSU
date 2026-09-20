package com.kanntan.su.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.data.repository.ModuleRepositoryImpl
import me.weishu.kernelsu.ui.screen.home.HomeUiState as UpstreamHomeUiState
import me.weishu.kernelsu.ui.viewmodel.HomeViewModel as UpstreamHomeViewModel

/**
 * Home ViewModel - binds real-time data from the system and ksud.
 *
 * All real data (kernel version, SELinux, KSU/manager state, versions, counts) is
 * produced by the upstream engine, which talks to [Natives] and the kernel. This
 * class maps that into the KanntanSU [HomeUiState] and owns the menu/navigation state.
 */
class HomeViewModel : ViewModel() {

    private val delegate = UpstreamHomeViewModel()

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // The upstream state flow emits a real, freshly-built state immediately and again
        // on every refresh; map every emission into the KanntanSU UI state.
        viewModelScope.launch {
            delegate.uiState.collect { upstream -> mapAndPublish(upstream) }
        }
    }

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
     * Refresh all data.
     */
    fun refresh() {
        viewModelScope.launch { delegate.refresh() }
    }

    private suspend fun mapAndPublish(upstream: UpstreamHomeUiState) {
        val isManager = upstream.isManager
        // Counting modules/roots touches ksud; keep it off the main thread.
        val moduleCount = withContext(Dispatchers.IO) {
            if (!isManager) return@withContext 0
            runCatching {
                ModuleRepositoryImpl().getModules().getOrDefault(emptyList()).size
            }.getOrDefault(0)
        }
        val superuserCount = withContext(Dispatchers.IO) {
            if (!isManager) return@withContext 0
            runCatching { Natives.getSuperuserCount() }.getOrDefault(0)
        }

        _uiState.update { current ->
            current.copy(
                systemInfo = SystemInfo(
                    kernelVersion = upstream.systemInfo.kernelVersion,
                    managerVersion = upstream.systemInfo.managerVersion,
                    fingerprint = upstream.systemInfo.fingerprint,
                    selinuxStatus = upstream.systemInfo.selinuxStatus,
                    seccompStatus = upstream.systemInfo.seccompStatus,
                    ksuStatus = determineKSUStatus(
                        ksuVersion = upstream.ksuVersion,
                        isRootAvailable = upstream.isRootAvailable,
                        isManager = isManager,
                        requiresNewKernel = upstream.requiresNewKernel
                    )
                ),
                ksuVersion = upstream.ksuVersion,
                isRootAvailable = upstream.isRootAvailable,
                isSafeMode = upstream.isSafeMode,
                isLateLoadMode = upstream.isLateLoadMode,
                isManager = isManager,
                superuserCount = superuserCount,
                moduleCount = moduleCount
            )
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
        _uiState.update { it.copy(navigateTo = screen, menuState = HomeUiState.MenuState.HIDDEN) }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(navigateTo = null) }
    }
}

/**
 * Preview helper
 */
@Suppress("unused")
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
