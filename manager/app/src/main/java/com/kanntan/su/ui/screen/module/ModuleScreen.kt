package com.kanntan.su.ui.screen.module

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Switch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kanntan.su.ui.component.dialog.UninstallDialog
import com.kanntan.su.ui.theme.kanntanColors
import com.kanntan.su.ui.theme.kanntanSwitchColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.data.model.Module
import me.weishu.kernelsu.ui.screen.module.ModuleActions
import me.weishu.kernelsu.ui.util.FlashResult
import me.weishu.kernelsu.ui.util.flashModule
import me.weishu.kernelsu.ui.viewmodel.ModuleViewModel

@Composable
fun ModuleScreen(
    viewModel: ModuleViewModel = viewModel(),
    actions: ModuleActions = ModuleActions(
        onRefresh = {},
        onSearchStatusChange = {},
        onSearchTextChange = {},
        onClearSearch = {},
        onRequestUpdateConfirmation = { _, _ -> },
        onRequestUninstallConfirmation = {},
        onDismissConfirmRequest = {},
        onConfirmUpdate = {},
        onOpenRepo = {},
        onToggleSortActionFirst = {},
        onToggleSortEnabledFirst = {},
        onOpenWebUi = {},
        onToggleModule = {},
        onUninstallModule = {},
        onUndoUninstallModule = {},
        onOpenFlash = {},
        onExecuteModuleAction = {}
    ),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showUninstallDialog by remember { mutableStateOf<Module?>(null) }
    var lastUninstalled by remember { mutableStateOf<Module?>(null) }
    val colors = kanntanColors()
    val scope = rememberCoroutineScope()

    // Module install: pick a flashable zip via SAF, stream ksud's output into a dialog.
    var installRunning by remember { mutableStateOf(false) }
    var installOutput by remember { mutableStateOf("") }
    var installResult by remember { mutableStateOf<FlashResult?>(null) }

    val zipPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        installOutput = ""
        installResult = null
        installRunning = true
        // flashModule blocks on a root shell, so it must never run on the main thread.
        scope.launch {
            val output = StringBuilder()
            val result = withContext(Dispatchers.IO) {
                flashModule(
                    uri = uri,
                    onStdout = { output.appendLine(it); installOutput = output.toString() },
                    onStderr = { output.appendLine(it); installOutput = output.toString() }
                )
            }
            installResult = result
            installRunning = false
            if (result.code == 0) {
                viewModel.fetchModuleList(checkUpdate = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initializePreferences()
        viewModel.fetchModuleList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.secondaryColor)
    ) {
        ModuleHeader(
            onBackClick = onNavigateBack,
            onRefreshClick = { viewModel.fetchModuleList(checkUpdate = true) },
            onInstallClick = { zipPicker.launch("application/zip") }
        )

        // Undo strip: uninstalling a module is reversible until reboot, so offer a one-tap revert.
        lastUninstalled?.let { mod ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primaryColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "已卸载 ${mod.name}",
                    color = colors.onPrimaryColor,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "撤销",
                    color = colors.onPrimaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel.undoUninstallModule(mod)
                        lastUninstalled = null
                    }.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        if (uiState.isRefreshing && !uiState.hasLoaded) {
            LoadingContent()
        } else if (uiState.moduleList.isEmpty()) {
            EmptyContent(onInstallClick = { zipPicker.launch("application/zip") })
        } else {
            ModuleList(
                modules = uiState.moduleList,
                onModuleClick = { module -> viewModel.toggleModule(module) },
                onModuleUninstall = { module -> showUninstallDialog = module },
                onOpenWebUi = { module -> actions.onOpenWebUi(module) },
                onExecuteAction = { module -> actions.onExecuteModuleAction(module) }
            )
        }
    }

    showUninstallDialog?.let { module ->
        UninstallDialog(
            moduleName = module.name,
            onConfirm = {
                viewModel.uninstallModule(module)
                lastUninstalled = module
                showUninstallDialog = null
            },
            onDismiss = { showUninstallDialog = null }
        )
    }

    if (installRunning || installResult != null) {
        InstallModuleDialog(
            running = installRunning,
            output = installOutput,
            result = installResult,
            onDismiss = {
                installOutput = ""
                installResult = null
            }
        )
    }
}

@Composable
private fun ModuleHeader(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onInstallClick: () -> Unit
) {
    val colors = kanntanColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.primaryColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.onPrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Modules",
                color = colors.onPrimaryColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onInstallClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Install module",
                    tint = colors.onPrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = colors.onPrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    val colors = kanntanColors()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Loading...",
            color = colors.onSecondaryColor,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun EmptyContent(onInstallClick: () -> Unit = {}) {
    val colors = kanntanColors()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No Modules",
                color = colors.onSecondaryColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Install a module to get started",
                color = colors.onSecondaryColor,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onInstallClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryColor,
                    contentColor = colors.onPrimaryColor
                )
            ) {
                Text(text = "安装模块", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ModuleList(
    modules: List<Module>,
    onModuleClick: (Module) -> Unit,
    onModuleUninstall: (Module) -> Unit,
    onOpenWebUi: (Module) -> Unit,
    onExecuteAction: (Module) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(modules, key = { it.id }) { module ->
            ModuleListItem(
                module = module,
                onClick = { onModuleClick(module) },
                onUninstall = { onModuleUninstall(module) },
                onOpenWebUi = { onOpenWebUi(module) },
                onExecuteAction = { onExecuteAction(module) }
            )
        }
    }
}

@Composable
private fun ModuleListItem(
    module: Module,
    onClick: () -> Unit,
    onUninstall: () -> Unit,
    onOpenWebUi: () -> Unit,
    onExecuteAction: () -> Unit
) {
    val colors = kanntanColors()
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.secondaryColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (module.metamodule) {
                                Text(
                                    text = "META",
                                    color = colors.onPrimaryColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(colors.primaryColor, RoundedCornerShape(2.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = module.name,
                            color = colors.onSecondaryColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "版本: ${module.version}  作者: ${module.author}",
                            color = colors.onSecondaryColor.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = module.enabled,
                    onCheckedChange = { onClick() },
                    colors = kanntanSwitchColors()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = module.description,
                color = colors.onSecondaryColor.copy(alpha = 0.7f),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (module.hasActionScript) {
                    ActionButton(
                        text = "执行",
                        icon = Icons.Default.PlayArrow,
                        onClick = onExecuteAction
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (module.hasWebUi) {
                    ActionButton(
                        text = "打开",
                        icon = Icons.Default.Public,
                        onClick = onOpenWebUi
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                ActionButton(
                    text = "卸载",
                    icon = Icons.Default.Delete,
                    onClick = onUninstall
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = kanntanColors()
    Box(
        modifier = Modifier
            .background(colors.secondaryColor, RoundedCornerShape(0.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.onSecondaryColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = colors.onSecondaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Streams ksud's `module install` output. ksud writes progress to stdout/stderr as it
 * goes, so showing the live log beats a bare spinner — and on failure the exit code and
 * stderr are right there.
 */
@Composable
private fun InstallModuleDialog(
    running: Boolean,
    output: String,
    result: FlashResult?,
    onDismiss: () -> Unit
) {
    val colors = kanntanColors()
    Dialog(onDismissRequest = { if (!running) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.secondaryColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = when {
                        running -> "正在安装模块…"
                        result != null && result.code == 0 -> "安装完成"
                        else -> "安装失败"
                    },
                    color = colors.onSecondaryColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!running && result != null && result.err.isNotBlank()) {
                    Text(
                        text = result.err,
                        color = colors.onSecondaryColor.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (running) {
                    LinearProgressIndicator(
                        color = colors.primaryColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Text(
                    text = output.ifBlank { if (running) "等待输出…" else "(无输出)" },
                    color = colors.onSecondaryColor.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState())
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        enabled = !running,
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.primaryColor)
                    ) {
                        Text(text = "关闭", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
