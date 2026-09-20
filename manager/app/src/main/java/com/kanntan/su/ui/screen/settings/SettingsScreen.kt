package com.kanntan.su.ui.screen.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanntan.su.ui.component.SegmentedSwitchItem
import com.kanntan.su.ui.theme.kanntanColors
import me.weishu.kernelsu.ui.screen.settings.SettingsUiState
import me.weishu.kernelsu.ui.viewmodel.SettingsViewModel

data class SettingsActions(
    val onReboot: () -> Unit = {},
    val onRestoreBoot: () -> Unit = {},
    val onSendLog: (android.content.Context) -> Unit = { _ -> },
    val onOpenThemeCustomization: () -> Unit = {},
    val onOpenAppProfileTemplate: () -> Unit = {},
    val onUninstallKernelSU: () -> Unit = {}
)

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    actions: SettingsActions,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = kanntanColors()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(colors.secondaryColor)) {
        SettingsHeader(onNavigateBack = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SettingsSection(title = "Update") {
                SettingsSwitchItem(
                    icon = Icons.Default.Update,
                    title = "检查更新",
                    summary = "在应用启动后, 自动检查是否有最新版本",
                    checked = uiState.checkUpdate,
                    onCheckedChange = { viewModel.setCheckUpdate(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSwitchItem(
                    icon = Icons.Default.GetApp,
                    title = "检查模块更新",
                    summary = "在应用启动后, 自动检查是否有最新版本的模块",
                    checked = uiState.checkModuleUpdate,
                    onCheckedChange = { viewModel.setCheckModuleUpdate(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = "Theme") {
                SettingsClickItem(
                    icon = Icons.Default.Palette,
                    title = "自定义主题",
                    summary = "更改您的主页, 背景和强调色",
                    onClick = { actions.onOpenThemeCustomization() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = "App Profile") {
                SettingsClickItem(
                    icon = Icons.Default.Apps,
                    title = "App Profile 模板",
                    summary = "管理本地和在线的App Profile 模板",
                    onClick = { actions.onOpenAppProfileTemplate() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = "Feature") {
                SettingsSwitchItem(
                    icon = Icons.Default.Terminal,
                    title = "传统 SU 命令支持",
                    summary = "允许通过 /system/bin/su 获取 Root 权限",
                    checked = uiState.suCompatMode == 1,
                    onCheckedChange = { viewModel.setSuCompatMode(if (it) 1 else 0) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSwitchItem(
                    icon = Icons.Default.LayersClear,
                    title = "卸载模块 (内核层面)",
                    summary = "在内核层面上, 针对需要的应用卸载模块",
                    checked = uiState.isKernelUmountEnabled,
                    onCheckedChange = { viewModel.setKernelUmountEnabled(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSwitchItem(
                    icon = Icons.Default.Article,
                    title = "SU 日志",
                    summary = "将与 Root 相关的事件记录到 KernelSU sulog 日志文件中",
                    checked = uiState.isSulogEnabled,
                    onCheckedChange = { viewModel.setSulogEnabled(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSwitchItem(
                    icon = Icons.Default.Adb,
                    title = "ADB Root",
                    summary = "以 Root 权限运行 adbd 守护进程",
                    checked = uiState.isAdbRootEnabled,
                    onCheckedChange = { viewModel.setAdbRootEnabled(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSwitchItem(
                    icon = Icons.Default.Unarchive,
                    title = "默认卸载模块",
                    summary = "启用后会将所有未自定义 Profile 的应用移除所有模块对系统的修改",
                    checked = uiState.isDefaultUmountModules,
                    onCheckedChange = { viewModel.setDefaultUmountModules(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSwitchItem(
                    icon = Icons.Default.FlashOn,
                    title = "自动越狱",
                    summary = "开机检测到 SELinux 宽容模式 自动使用 Magica 提权。需要授予本应用自启动权限",
                    enabled = uiState.isLateLoadMode,
                    checked = uiState.autoJailbreak,
                    onCheckedChange = { viewModel.setAutoJailbreak(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsClickItem(
                    icon = Icons.Default.Delete,
                    title = "卸载 KernelSU",
                    summary = "选项卸载或还原 KernelSU 对你设备的改动",
                    onClick = { actions.onUninstallKernelSU() }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsClickItem(
                    icon = Icons.Default.BugReport,
                    title = "发送日志",
                    summary = "将 KernelSU 日志文件保存在手机内或者分享给别人",
                    onClick = { actions.onSendLog(context) }
                )
            }
        }
    }
}

@Composable
private fun SettingsHeader(onNavigateBack: () -> Unit) {
    val colors = kanntanColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.primaryColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.onPrimaryColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Setting",
            color = colors.onPrimaryColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = kanntanColors()
    Column {
        Text(
            text = title,
            color = colors.onSecondaryColor.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.secondaryColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) { content() }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    summary: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = kanntanColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = { onCheckedChange(!checked) })
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(colors.primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onPrimaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = if (enabled) colors.onSecondaryColor
                        else colors.onSecondaryColor.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = summary,
                    color = colors.onSecondaryColor.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // Material Design 2 switch (not the MD3 component).
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onPrimaryColor,
                checkedTrackColor = colors.primaryColor,
                uncheckedThumbColor = colors.primaryColor,
                uncheckedTrackColor = colors.secondaryColor.copy(alpha = 0.4f),
                disabledCheckedThumbColor = colors.onPrimaryColor,
                disabledUncheckedThumbColor = colors.onSecondaryColor.copy(alpha = 0.4f),
                disabledCheckedTrackColor = colors.primaryColor.copy(alpha = 0.4f),
                disabledUncheckedTrackColor = colors.secondaryColor.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    summary: String,
    onClick: () -> Unit
) {
    val colors = kanntanColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(colors.primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onPrimaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = colors.onSecondaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = summary,
                    color = colors.onSecondaryColor.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.onSecondaryColor.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
