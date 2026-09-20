package com.kanntan.su.ui.screen.module

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kanntan.su.ui.component.dialog.UninstallDialog
import com.kanntan.su.ui.theme.kanntanColors
import me.weishu.kernelsu.data.model.Module
import me.weishu.kernelsu.ui.screen.module.ModuleActions
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
            onRefreshClick = { viewModel.fetchModuleList(checkUpdate = true) }
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
            EmptyContent()
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
}

@Composable
private fun ModuleHeader(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit
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
private fun EmptyContent() {
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
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.onPrimaryColor,
                        checkedTrackColor = colors.primaryColor,
                        uncheckedThumbColor = colors.primaryColor,
                        uncheckedTrackColor = colors.secondaryColor.copy(alpha = 0.4f)
                    )
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
