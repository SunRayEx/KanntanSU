package com.kanntan.su

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kanntan.su.ui.screen.home.HomeScreen
import com.kanntan.su.ui.screen.home.HomeActions
import com.kanntan.su.ui.screen.home.HomeViewModel
import com.kanntan.su.ui.screen.module.ModuleScreen
import com.kanntan.su.ui.screen.modulerepo.ModuleRepoScreen
import com.kanntan.su.ui.screen.template.TemplateScreen
import com.kanntan.su.ui.screen.flash.FlashScreen
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel
import me.weishu.kernelsu.ui.viewmodel.ModuleRepoViewModel
import me.weishu.kernelsu.ui.viewmodel.TemplateViewModel
import me.weishu.kernelsu.ui.screen.module.ModuleActions
import me.weishu.kernelsu.ui.viewmodel.SuperUserViewModel
import me.weishu.kernelsu.ui.screen.superuser.SuperUserActions
import com.kanntan.su.ui.screen.superuser.SuperUserScreen
import com.kanntan.su.ui.screen.settings.SettingsScreen
import com.kanntan.su.ui.screen.settings.SettingsViewModel
import com.kanntan.su.ui.screen.settings.SettingsActions
import com.kanntan.su.ui.screen.sulog.SulogScreen
import me.weishu.kernelsu.ui.viewmodel.SulogViewModel
import me.weishu.kernelsu.ui.screen.sulog.SulogActions
import com.kanntan.su.ui.screen.install.InstallScreen
import com.kanntan.su.ui.screen.install.InstallViewModel
import com.kanntan.su.ui.screen.install.InstallActions
import com.kanntan.su.ui.screen.appprofile.AppProfileScreen
import com.kanntan.su.ui.screen.appprofile.AppProfileActions
import com.kanntan.su.ui.screen.colorpalette.ColorPaletteScreen
import com.kanntan.su.ui.theme.KanntanThemeState
import com.kanntan.su.ui.theme.LocalKanntanTheme
import com.kanntan.su.ui.theme.KanntanSUTheme
import com.kanntan.su.ui.util.reboot
import android.util.Log
import android.content.Intent
import android.widget.Toast
import me.weishu.kernelsu.ui.screen.flash.FlashIt
import me.weishu.kernelsu.ui.util.LkmSelection

enum class Screen {
    HOME, MODULES, SUPERUSER, SETTINGS, SULOG, INSTALL, APP_PROFILE, COLOR_PALETTE,
    MODULE_REPO, TEMPLATE, FLASH
}

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        
        setContent {
            KanntanSUTheme(darkTheme = false, wallpaperEnabled = false) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
                    MainNavigation()
                }
            }
        }
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }
}

@Composable
private fun MainNavigation() {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedPackageName by remember { mutableStateOf<String?>(null) }
    var flashIt by remember { mutableStateOf<FlashIt?>(null) }
    val context = LocalContext.current
    val themeState = remember { KanntanThemeState(context) }
    val colors by themeState.colors.collectAsState()
    
    val homeViewModel: HomeViewModel = viewModel()
    val moduleViewModel: ModuleViewModel = viewModel()
    val moduleRepoViewModel: ModuleRepoViewModel = viewModel()
    val templateViewModel: TemplateViewModel = viewModel()
    val superUserViewModel: SuperUserViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val sulogViewModel: SulogViewModel = viewModel()
    val installViewModel: InstallViewModel = viewModel()
    
    CompositionLocalProvider(LocalKanntanTheme provides themeState) {
        Box(modifier = Modifier.fillMaxSize().background(colors.middleColor)) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    if (targetState == Screen.HOME) {
                        // returning to root: plain fade, root has no elevation
                        fadeIn(animationSpec = tween(250)) togetherWith
                            fadeOut(animationSpec = tween(250))
                    } else {
                        // pushed page rises in slightly: gives a sense of layer/depth
                        slideInVertically(animationSpec = tween(300)) { fullHeight -> fullHeight / 10 } +
                            fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(200))
                    }
                },
                label = "kanntan-screen"
            ) { screen ->
                val isRoot = screen == Screen.HOME
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isRoot) Modifier else Modifier.shadow(elevation = 8.dp))
                ) {
                    when (screen) {
            Screen.HOME -> {
                HomeScreen(
                    viewModel = homeViewModel,
                    actions = HomeActions(
                        onInstallClick = { currentScreen = Screen.INSTALL },
                        onModulesClick = { currentScreen = Screen.MODULES },
                        onApplicationClick = { currentScreen = Screen.SUPERUSER },
                        onSettingClick = { currentScreen = Screen.SETTINGS },
                        onMenuToggle = {},
                        onMenuDismiss = {},
                        onJailbreakClick = {
                            try {
                                context.startService(Intent(context, com.kanntan.su.magica.MagicaService::class.java))
                                Toast.makeText(context, "正在越狱...", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Failed to start MagicaService", e)
                                Toast.makeText(context, "越狱失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ),
                    wallpaperBitmap = null
                )
            }
            
            Screen.MODULES -> {
                ModuleScreen(
                    viewModel = moduleViewModel,
                    actions = ModuleActions(
                        onRefresh = { moduleViewModel.fetchModuleList(checkUpdate = true) },
                        onSearchStatusChange = { moduleViewModel.updateSearchStatus(it) },
                        onSearchTextChange = { moduleViewModel.updateSearchText(it) },
                        onClearSearch = { moduleViewModel.updateSearchText("") },
                        onRequestUpdateConfirmation = { module, info -> moduleViewModel.requestUpdateConfirmation(module, info) },
                        onRequestUninstallConfirmation = { module -> moduleViewModel.requestUninstallConfirmation(module) },
                        onDismissConfirmRequest = { moduleViewModel.dismissConfirmRequest() },
                        onConfirmUpdate = {},
                        onOpenRepo = { currentScreen = Screen.MODULE_REPO },
                        onToggleSortActionFirst = { moduleViewModel.toggleSortActionFirst() },
                        onToggleSortEnabledFirst = { moduleViewModel.toggleSortEnabledFirst() },
                        onOpenWebUi = { /* WebUI opened via Intent, not navigation */ },
                        onToggleModule = { module -> moduleViewModel.toggleModule(module) },
                        onUninstallModule = { module -> moduleViewModel.uninstallModule(module) },
                        onUndoUninstallModule = { module -> moduleViewModel.undoUninstallModule(module) },
                        onOpenFlash = { uris ->
                            flashIt = FlashIt.FlashModules(uris)
                            currentScreen = Screen.FLASH
                        },
                        onExecuteModuleAction = { /* TODO: execute module action */ }
                    ),
                    onNavigateBack = { currentScreen = Screen.HOME }
                )
            }
            
            Screen.MODULE_REPO -> {
                ModuleRepoScreen(
                    viewModel = moduleRepoViewModel,
                    onNavigateBack = { currentScreen = Screen.MODULES }
                )
            }
            
            Screen.TEMPLATE -> {
                TemplateScreen(
                    viewModel = templateViewModel,
                    onNavigateBack = { currentScreen = Screen.SETTINGS },
                    onImport = { /* TODO: import templates */ },
                    onExport = { /* TODO: export templates */ }
                )
            }
            
            Screen.FLASH -> {
                flashIt?.let { flash ->
                    FlashScreen(
                        flashIt = flash,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                }
            }
            
            Screen.SUPERUSER -> {
                SuperUserScreen(
                    viewModel = superUserViewModel,
                    actions = SuperUserActions(
                        onRefresh = { superUserViewModel.loadAppList(force = true) },
                        onOpenSulog = { currentScreen = Screen.SULOG },
                        onSearchTextChange = { superUserViewModel.updateSearchText(it) },
                        onSearchStatusChange = { superUserViewModel.updateSearchStatus(it) },
                        onClearSearch = { superUserViewModel.updateSearchText("") },
                        onUpdateSortConfig = { superUserViewModel.updateSortConfig(it) },
                        onToggleShowSystemApps = { superUserViewModel.toggleShowSystemApps() },
                        onToggleShowOnlyPrimaryUserApps = { superUserViewModel.toggleShowOnlyPrimaryUserApps() },
                        onOpenProfile = { group ->
                            selectedPackageName = group.primary.packageName
                            currentScreen = Screen.APP_PROFILE
                        }
                    ),
                    onNavigateBack = { currentScreen = Screen.HOME }
                )
            }
            
            Screen.SETTINGS -> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    actions = SettingsActions(
                        onReboot = { reboot() },
                        onRestoreBoot = { reboot("recovery") },
                        onSendLog = { /* TODO */ },
                        onOpenThemeCustomization = { currentScreen = Screen.COLOR_PALETTE },
                        onOpenAppProfileTemplate = { currentScreen = Screen.TEMPLATE },
                        onUninstallKernelSU = {
                            Toast.makeText(context, "卸载 KernelSU 功能开发中", Toast.LENGTH_SHORT).show()
                        }
                    ),
                    onNavigateBack = { currentScreen = Screen.HOME }
                )
            }
            
            Screen.SULOG -> {
                SulogScreen(
                    viewModel = sulogViewModel,
                    actions = SulogActions(
                        onBack = { currentScreen = Screen.HOME },
                        onRefresh = { sulogViewModel.refreshLatest() },
                        onEnableSulog = { sulogViewModel.enableSulog() },
                        onCleanFile = { sulogViewModel.cleanFile() },
                        onSearchTextChange = { sulogViewModel.setSearchText(it) },
                        onToggleFilter = { sulogViewModel.toggleFilter(it) },
                        onSelectFile = { sulogViewModel.refresh(it) }
                    ),
                    onNavigateBack = { currentScreen = Screen.HOME }
                )
            }
            
            Screen.INSTALL -> {
                InstallScreen(
                    viewModel = installViewModel,
                    actions = InstallActions(
                        onInstall = { installViewModel.startInstall() },
                        onSelectMethod = { method -> installViewModel.selectMethod(method) }
                    ),
                    onNavigateBack = { currentScreen = Screen.HOME }
                )
            }
            
            Screen.APP_PROFILE -> {
                selectedPackageName?.let { packageName ->
                    AppProfileScreen(
                        packageName = packageName,
                        actions = AppProfileActions(
                            onSave = { },
                            onReset = { }
                        ),
                        onNavigateBack = { 
                            selectedPackageName = null
                            currentScreen = Screen.SUPERUSER
                        }
                    )
                }
            }
            
            Screen.COLOR_PALETTE -> {
                ColorPaletteScreen(
                    onNavigateBack = { currentScreen = Screen.SETTINGS }
                )
            }
        }
                }
            }
        }
    }
}
