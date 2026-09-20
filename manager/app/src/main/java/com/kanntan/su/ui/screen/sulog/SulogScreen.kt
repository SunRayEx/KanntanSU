package com.kanntan.su.ui.screen.sulog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanntan.su.ui.theme.kanntanColors
import me.weishu.kernelsu.ui.screen.sulog.SulogActions
import me.weishu.kernelsu.ui.screen.sulog.SulogScreenState
import me.weishu.kernelsu.ui.util.SulogEntry
import me.weishu.kernelsu.ui.viewmodel.SulogViewModel

@Composable
fun SulogScreen(
    viewModel: SulogViewModel,
    actions: SulogActions,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = kanntanColors()

    LaunchedEffect(Unit) {
        viewModel.refreshLatest()
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.secondaryColor)) {
        SulogHeader(onNavigateBack = onNavigateBack)

        if (uiState.isLoading && uiState.entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primaryColor, strokeWidth = 3.dp)
            }
        } else if (uiState.entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No Logs", color = colors.onSecondaryColor, fontSize = 18.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(uiState.visibleEntries, key = { it.key }) { entry ->
                    SulogItem(entry = entry)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SulogHeader(onNavigateBack: () -> Unit) {
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
        Text("Logs", color = colors.onPrimaryColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SulogItem(entry: SulogEntry) {
    val colors = kanntanColors()
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.secondaryColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = entry.timestampText ?: "",
                color = colors.onSecondaryColor.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.key,
                color = colors.onSecondaryColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            entry.fields.forEach { (key, value) ->
                Text(
                    text = "$key: $value",
                    color = colors.onSecondaryColor.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
