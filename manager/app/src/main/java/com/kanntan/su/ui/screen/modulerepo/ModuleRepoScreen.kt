package com.kanntan.su.ui.screen.modulerepo

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanntan.su.ui.theme.kanntanColors
import me.weishu.kernelsu.data.model.RepoModule
import me.weishu.kernelsu.ui.viewmodel.ModuleRepoViewModel

@Composable
fun ModuleRepoScreen(
    viewModel: ModuleRepoViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchText by remember { mutableStateOf("") }
    val colors = kanntanColors()

    LaunchedEffect(Unit) {
        if (uiState.modules.isEmpty()) {
            viewModel.refresh()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.secondaryColor)) {
        ModuleRepoHeader(onNavigateBack = onNavigateBack)

        ModuleRepoSearchBar(
            searchText = searchText,
            onSearchTextChange = {
                searchText = it
                viewModel.updateSearchText(it)
            }
        )

        if (uiState.isRefreshing && uiState.modules.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primaryColor, strokeWidth = 3.dp)
            }
        } else if (!uiState.isRefreshing && uiState.modules.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (uiState.offline) "Network Offline" else "No Modules Found",
                        color = colors.onSecondaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.offline) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .background(colors.primaryColor)
                                .clickable(onClick = { viewModel.refresh() })
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
            val displayModules = if (searchText.isNotEmpty()) {
                uiState.searchResults
            } else {
                uiState.modules
            }

            if (displayModules.isEmpty() && searchText.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No Results",
                        color = colors.onSecondaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(displayModules, key = { it.moduleId }) { module ->
                        ModuleRepoItem(module = module)
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ModuleRepoHeader(onNavigateBack: () -> Unit) {
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
            Text("<", color = colors.onPrimaryColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Module Repository",
            color = colors.onPrimaryColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ModuleRepoSearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    val colors = kanntanColors()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.secondaryColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        BasicTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8E8E8), RoundedCornerShape(0.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            textStyle = TextStyle(
                color = colors.onSecondaryColor,
                fontSize = 14.sp
            ),
            singleLine = true,
            cursorBrush = SolidColor(colors.primaryColor),
            decorationBox = { innerTextField ->
                Box {
                    if (searchText.isEmpty()) {
                        Text(
                            text = "Search modules...",
                            color = colors.onSecondaryColor.copy(alpha = 0.4f),
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun ModuleRepoItem(module: RepoModule) {
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
                text = module.moduleName,
                color = colors.onSecondaryColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "ID: ${module.moduleId}",
                color = colors.onSecondaryColor.copy(alpha = 0.7f),
                fontSize = 12.sp
            )

            Text(
                text = "Author: ${module.authors}",
                color = colors.onSecondaryColor.copy(alpha = 0.7f),
                fontSize = 12.sp
            )

            if (module.summary.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = module.summary,
                    color = colors.onSecondaryColor.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (module.stargazerCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "*",
                            color = colors.onSecondaryColor.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = module.stargazerCount.toString(),
                            color = colors.onSecondaryColor.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }

                if (module.latestReleaseTime.isNotEmpty()) {
                    Text(
                        text = module.latestReleaseTime,
                        color = colors.onSecondaryColor.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
