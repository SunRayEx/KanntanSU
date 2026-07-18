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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kanntan.su.ui.component.dialog.UninstallDialog
import com.kanntan.su.ui.theme.ContentBackground
import com.kanntan.su.ui.theme.PureBlack
import com.kanntan.su.ui.theme.PureWhite
import com.kanntan.su.ui.theme.TextOnBlack
import com.kanntan.su.ui.theme.TextOnWhite
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
        onConsumeEffect = {},
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

    LaunchedEffect(Unit) {
        viewModel.initializePreferences()
        viewModel.fetchModuleList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ContentBackground)
    ) {
        ModuleHeader(
            onBackClick = onNavigateBack,
            onRefreshClick = { viewModel.fetchModuleList(checkUpdate = true) }
        )

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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PureBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(PureWhite)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "<",
                    color = TextOnBlack,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Modules",
                color = TextOnBlack,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(PureWhite)
                .clickable(onClick = onRefreshClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "≡",
                color = TextOnBlack,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Loading...",
            color = TextOnWhite,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No Modules",
                color = TextOnWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Install a module to get started",
                color = TextOnWhite,
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
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
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
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PureBlack)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (module.metamodule) {
                                Text(
                                    text = "META",
                                    color = PureWhite,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(PureBlack, RoundedCornerShape(2.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = module.name,
                            color = TextOnWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "版本: ${module.version}  作者: ${module.author}",
                            color = TextOnWhite.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(if (module.enabled) PureBlack else PureWhite)
                        .clickable(onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (module.enabled) "ON" else "OFF",
                        color = if (module.enabled) TextOnBlack else TextOnWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = module.description,
                color = TextOnWhite.copy(alpha = 0.7f),
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
                        icon = "▶",
                        onClick = onExecuteAction
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (module.hasWebUi) {
                    ActionButton(
                        text = "打开",
                        icon = "<>",
                        onClick = onOpenWebUi
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                ActionButton(
                    text = "卸载",
                    icon = "■",
                    onClick = onUninstall
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(PureWhite, RoundedCornerShape(0.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                color = TextOnWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = TextOnWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
