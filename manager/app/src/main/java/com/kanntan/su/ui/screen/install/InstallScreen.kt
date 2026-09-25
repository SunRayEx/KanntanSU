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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanntan.su.ui.theme.kanntanColors
import com.kanntan.su.ui.util.getAvailablePartitions
import com.kanntan.su.ui.util.getDefaultPartition
import com.kanntan.su.ui.util.installBoot
import com.kanntan.su.ui.util.rootAvailable
import me.weishu.kernelsu.ui.screen.install.isKoFile
import com.kanntan.su.ui.util.FlashResult
import me.weishu.kernelsu.ui.util.LkmSelection
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.window.Dialog
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

    // ksud 的实时输出。libsu 的回调是串行的，所以用一个 buffer 拼接再原子赋值即可。
    var flashOutput by remember { mutableStateOf("") }
    var flashRunning by remember { mutableStateOf(false) }
    var flashResult by remember { mutableStateOf<FlashResult?>(null) }
    var showOutputDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

            if (flashRunning) {
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
                            if (!rootAvailable()) {
                                Toast.makeText(context, "没有 Root 权限，无法直接刷入", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            flashOutput = ""
                            flashResult = null
                            flashRunning = true
                            showOutputDialog = true
                            scope.launch(Dispatchers.IO) {
                                val bootUri = when (method) {
                                    is InstallMethod.SelectFile -> method.uri
                                    else -> null
                                }
                                val output = StringBuilder()
                                val result = installBoot(
                                    bootUri = bootUri,
                                    lkm = uiState.lkmSelection,
                                    ota = method == InstallMethod.DirectInstallToInactiveSlot,
                                    partition = partitions.getOrNull(partitionSelectionIndex),
                                    allowShell = uiState.allowShell,
                                    enableAdb = uiState.enableAdb,
                                    onStdout = { line ->
                                        output.appendLine(line)
                                        flashOutput = output.toString()
                                    },
                                    onStderr = { line ->
                                        output.appendLine(line)
                                        flashOutput = output.toString()
                                    },
                                )
                                withContext(Dispatchers.Main) {
                                    flashResult = result
                                    flashRunning = false
                                    if (result.isSuccess) {
                                        Toast.makeText(context, "刷入成功", Toast.LENGTH_SHORT).show()
                                    } else {
                                        errorMessage = result.error.ifBlank { "未知错误（ksud 无输出）" }
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

    if (showOutputDialog) {
        FlashOutputDialog(
            running = flashRunning,
            output = flashOutput,
            result = flashResult,
            onDismiss = {
                showOutputDialog = false
                if (flashResult?.isSuccess == true) onNavigateBack()
            }
        )
    }

    errorMessage?.let { message ->
        KanntanErrorDialog(
            message = message,
            onDismiss = { errorMessage = null }
        )
    }
}

@Composable
private fun FlashOutputDialog(
    running: Boolean,
    output: String,
    result: FlashResult?,
    onDismiss: () -> Unit
) {
    val colors = kanntanColors()
    val scrollState = rememberScrollState()
    LaunchedEffect(output) {
        // 跟随最新输出滚动到底部
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    Dialog(onDismissRequest = { if (!running) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.secondaryColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = when {
                        running -> "正在刷入…"
                        result != null && result.isSuccess -> "刷入成功"
                        result != null -> "刷入失败"
                        else -> "准备中…"
                    },
                    color = colors.onSecondaryColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!running && result != null && result.error.isNotBlank()) {
                    Text(
                        text = result.error,
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
                        .heightIn(max = 320.dp)
                        .verticalScroll(scrollState)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.TextButton(
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

@Composable
private fun KanntanErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    val colors = kanntanColors()
    AlertDialog(
        onDismissRequest = onDismiss,
        buttons = {
            androidx.compose.material.TextButton(onClick = onDismiss) {
                Text("确定", color = colors.primaryColor)
            }
        },
        title = { Text("刷入失败", color = colors.onSecondaryColor) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = message,
                    color = colors.onSecondaryColor,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        backgroundColor = colors.secondaryColor,
        contentColor = colors.onSecondaryColor
    )
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
