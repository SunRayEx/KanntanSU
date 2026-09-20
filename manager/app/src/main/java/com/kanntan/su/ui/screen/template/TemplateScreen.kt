package com.kanntan.su.ui.screen.template

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanntan.su.ui.theme.kanntanColors
import kotlinx.coroutines.launch
import me.weishu.kernelsu.data.model.TemplateInfo
import me.weishu.kernelsu.ui.viewmodel.TemplateViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TemplateScreen(
    viewModel: TemplateViewModel,
    onNavigateBack: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val colors = kanntanColors()

    LaunchedEffect(Unit) {
        if (uiState.templates.isEmpty()) {
            viewModel.fetchTemplates()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.secondaryColor)) {
        TemplateHeader(
            onNavigateBack = onNavigateBack,
            onImport = onImport,
            onExport = onExport
        )

        if (uiState.isRefreshing && uiState.templates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primaryColor, strokeWidth = 3.dp)
            }
        } else if (!uiState.isRefreshing && uiState.templates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (uiState.offline) "Network Offline" else "No Templates",
                        color = colors.onSecondaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.offline) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .background(colors.primaryColor)
                                .clickable(onClick = { scope.launch { viewModel.fetchTemplates() } })
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Retry",
                                color = colors.onPrimaryColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            val templates = uiState.templateList.ifEmpty { uiState.templates }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                itemsIndexed(templates, key = { _, t -> t.id }) { index, template ->
                    TemplateItem(
                        index = index,
                        template = template,
                        onClick = { /* TODO: open template editor */ }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun TemplateHeader(
    onNavigateBack: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit
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
                text = "App Profile Templates",
                color = colors.onPrimaryColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onImport) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Import",
                    tint = colors.onPrimaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onExport) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Export",
                    tint = colors.onPrimaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TemplateItem(
    index: Int,
    template: TemplateInfo,
    onClick: () -> Unit
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
            Text(
                text = template.name.ifEmpty { template.id },
                color = colors.onSecondaryColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = template.id + if (template.author.isEmpty()) "" else " @${template.author}",
                color = colors.onSecondaryColor.copy(alpha = 0.7f),
                fontSize = 12.sp
            )

            if (template.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = template.description,
                    color = colors.onSecondaryColor.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TagChip("UID: ${template.uid}")
                TagChip("GID: ${template.gid}")
                TagChip(template.context)
                TagChip(if (template.local) "local" else "remote")
            }
        }
    }
}

@Composable
private fun TagChip(label: String) {
    val colors = kanntanColors()
    Box(
        modifier = Modifier
            .background(colors.primaryColor, RoundedCornerShape(0.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = colors.onPrimaryColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
