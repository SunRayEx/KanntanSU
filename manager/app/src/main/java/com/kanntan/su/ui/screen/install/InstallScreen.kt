package com.kanntan.su.ui.screen.install

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanntan.su.ui.theme.kanntanColors
import com.kanntan.su.ui.util.getAvailablePartitions
import com.kanntan.su.ui.util.getDefaultPartition
import com.kanntan.su.ui.util.installBoot
import com.kanntan.su.ui.util.rootAvailable
import me.weishu.kernelsu.ui.screen.install.isKoFile
import me.weishu.kernelsu.ui.util.LkmSelection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Install Screen - LKM刷入界面
 * 支持选择分区：boot, init_boot, vendor_boot
 */
@Composable
fun InstallScreen(
    viewModel: InstallViewModel,
    actions: InstallActions,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isFlashing by rememberSaveable { mutableStateOf(false) }

    var partitionSelectionIndex by rememberSaveable { mutableIntStateOf(0) }
    var hasCustomSelected by rememberSaveable { mutableStateOf(false) }
    val colors = kanntanColors()

    val partitions by produceState(initialValue = emptyList()) { value = getAvailablePartitions() }
    val defaultPartition by produceState(initialValue = "") { value = getDefaultPartition() }

    val defaultIndex = remember(partitions, defaultPartition) {
        partitions.indexOf(defaultPartition).coerceAtLeast(0)
    }

    LaunchedEffect(partitions, defaultIndex, hasCustomSelected) {
        if (partitions.isEmpty()) return@LaunchedEffect
        if (!hasCustomSelected) {
            partitionSelectionIndex = defaultIndex.coerceIn(0, partitions.lastIndex)
        } else if (partitionSelectionIndex > partitions.lastIndex) {
            partitionSelectionIndex = partitions.lastIndex
        }
    }

    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                viewModel.selectMethod(InstallMethod.SelectFile(uri, "选择的文件"))
            }
        }
    }

    val selectLkmLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                if (isKoFile(context, uri)) {
                    viewModel.selectLkm(LkmSelection.LkmUri(uri))
                } else {
                    Toast.makeText(context, "请选择 .ko 文件", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.secondaryColor)
    ) {
        InstallHeader(onNavigateBack = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            InstallSection(title = "安装方式") {
                InstallMethodItem(
                    title = "选择文件",
                    summary = "选择 boot.img 或其他分区镜像文件",
                    isSelected = uiState.installMethod is InstallMethod.SelectFile,
                    onClick = {
                        selectImageLauncher.launch(
                            Intent(Intent.ACTION_GET_CONTENT).apply {
                                type = "application/octet-stream"
                            }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                InstallMethodItem(
                    title = "直接安装",
                    summary = "直接刷入当前分区 (需要Root权限)",
                    isSelected = uiState.installMethod == InstallMethod.DirectInstall,
                    onClick = { viewModel.selectMethod(InstallMethod.DirectInstall) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            InstallSection(title = "选择分区") {
                partitions.forEachIndexed { index, partition ->
                    PartitionItem(
                        partition = partition,
                        isDefault = partition == defaultPartition,
                        isSelected = index == partitionSelectionIndex,
                        onClick = {
                            hasCustomSelected = true
                            partitionSelectionIndex = index
                        }
                    )
                    if (index < partitions.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            InstallSection(title = "LKM 模组 (可选)") {
                val hasLkm = uiState.lkmSelection is LkmSelection.LkmUri
                InstallMethodItem(
                    title = "选择 LKM 文件",
                    summary = if (hasLkm) "已选择" else "选择 .ko 文件",
                    isSelected = hasLkm,
                    onClick = {
                        selectLkmLauncher.launch(
                            Intent(Intent.ACTION_GET_CONTENT).apply {
                                type = "application/octet-stream"
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isFlashing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(colors.primaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = colors.onPrimaryColor,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "刷入中...",
                            color = colors.onPrimaryColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(colors.primaryColor)
                        .clickable {
                            val method = uiState.installMethod
                            if (method == null) {
                                Toast.makeText(context, "请先选择安装方式", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            isFlashing = true
                            scope.launch(Dispatchers.IO) {
                                val bootUri = when (method) {
                                    is InstallMethod.SelectFile -> method.uri
                                    else -> null
                                }
                                val result = installBoot(
                                    bootUri = bootUri,
                                    lkm = uiState.lkmSelection,
                                    ota = method == InstallMethod.DirectInstallToInactiveSlot,
                                    partition = partitions.getOrNull(partitionSelectionIndex),
                                    allowShell = false,
                                    enableAdb = false,
                                    onStdout = {},
                                    onStderr = {},
                                )
                                withContext(Dispatchers.Main) {
                                    isFlashing = false
                                    if (result.isSuccess) {
                                        Toast.makeText(context, "刷入成功", Toast.LENGTH_SHORT).show()
                                        onNavigateBack()
                                    } else {
                                        Toast.makeText(context, "刷入失败: ${result.error}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "开始刷入",
                        color = colors.onPrimaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun InstallHeader(onNavigateBack: () -> Unit) {
    val colors = kanntanColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.primaryColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(colors.secondaryColor)
                .clickable(onClick = onNavigateBack),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                color = colors.onSecondaryColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Install",
            color = colors.onPrimaryColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InstallSection(
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
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) { content() }
        }
    }
}

@Composable
private fun InstallMethodItem(
    title: String,
    summary: String,
    isSelected: Boolean,
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
        Column(modifier = Modifier.weight(1f)) {
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
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(if (isSelected) colors.primaryColor else colors.secondaryColor)
        )
    }
}

@Composable
private fun PartitionItem(
    partition: String,
    isDefault: Boolean,
    isSelected: Boolean,
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isDefault) "$partition (默认)" else partition,
                color = colors.onSecondaryColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(if (isSelected) colors.primaryColor else colors.secondaryColor)
        )
    }
}

data class InstallActions(
    val onInstall: () -> Unit = {},
    val onSelectMethod: (InstallMethod) -> Unit = {}
)
