package com.kanntan.su.ui.screen.colorpalette

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanntan.su.ui.theme.LocalKanntanTheme
import com.kanntan.su.ui.theme.kanntanColors

/**
 * Color Palette Screen - 主题自定义页面
 *
 * 自定义主页的：上半部分大按钮 / 中间内容部分 / 下半部分按钮，
 * 以及全局主题色（默认黑白，可替换为另外两种颜色）。
 * 选择即时生效并持久化。
 */
@Composable
fun ColorPaletteScreen(
    onNavigateBack: () -> Unit
) {
    val theme = LocalKanntanTheme.current
    val colors by theme.colors.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.middleColor)
    ) {
        // Header
        ColorPaletteHeader(onNavigateBack = onNavigateBack)

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 主页上半部分（大按钮区域）
            ColorSection(
                title = "主页上半部分",
                colors = sectionColorOptions,
                selectedColor = colors.topColor,
                onColorSelected = { theme.setTopColor(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 主页中间内容部分
            ColorSection(
                title = "主页中间内容部分",
                colors = sectionColorOptions,
                selectedColor = colors.middleColor,
                onColorSelected = { theme.setMiddleColor(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 主页下半部分（按钮区域）
            ColorSection(
                title = "主页下半部分",
                colors = sectionColorOptions,
                selectedColor = colors.bottomColor,
                onColorSelected = { theme.setBottomColor(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 主题色 #1（替换默认黑）
            ColorSection(
                title = "主题色 1（默认：黑）",
                colors = themeColorOptions,
                selectedColor = colors.primaryColor,
                onColorSelected = { theme.setPrimaryColor(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 主题色 #2（替换默认白）
            ColorSection(
                title = "主题色 2（默认：白）",
                colors = themeColorOptions,
                selectedColor = colors.secondaryColor,
                onColorSelected = { theme.setSecondaryColor(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 预览
            Text(
                text = "预览",
                color = colors.onMiddleColor.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            PreviewCard(colors)

            Spacer(modifier = Modifier.height(24.dp))

            // 恢复默认黑白
            Button(
                onClick = { theme.reset() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryColor,
                    contentColor = colors.onPrimaryColor
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "恢复默认（黑白）", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ColorPaletteHeader(onNavigateBack: () -> Unit) {
    val colors = kanntanColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.topColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.topColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "自定义主题",
            color = colors.onTopColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PreviewCard(colors: com.kanntan.su.ui.theme.KanntanColors) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.secondaryColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 顶部按钮区
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(colors.topColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "KanntanSU",
                    color = colors.onTopColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 中间内容区
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(colors.middleColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "中间内容部分",
                    color = colors.onMiddleColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 底部按钮区
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(colors.bottomColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "底部按钮",
                    color = colors.onBottomColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 主题色按钮示例
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(colors.primaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "主题色 1",
                        color = colors.onPrimaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(colors.secondaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "主题色 2",
                        color = colors.onSecondaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSection(
    title: String,
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val theme = kanntanColors()
    Column {
        Text(
            text = title,
            color = theme.onMiddleColor.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = theme.secondaryColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(colors) { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (color == selectedColor) 3.dp else 0.dp,
                                color = if (color == selectedColor) theme.primaryColor else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(color) }
                    )
                }
            }
        }
    }
}

// 主页三个分区的候选色（含默认黑/白）
private val sectionColorOptions = listOf(
    Color(0xFF000000),
    Color(0xFFFFFFFF),
    Color(0xFF1C1C1C),
    Color(0xFF333333),
    Color(0xFF4A4A4A),
    Color(0xFF8A8A8A),
    Color(0xFFC8C8C8),
    Color(0xFFE0E0E0),
    Color(0xFFF5F5F5),
    Color(0xFF1A237E),
    Color(0xFF0D47A1),
    Color(0xFF004D40),
    Color(0xFF1B5E20),
    Color(0xFF33691E),
    Color(0xFFE65100),
    Color(0xFF880E4F),
    Color(0xFF4A148C),
    Color(0xFFB71C1C),
)

// 主题色候选（默认黑/白 + 常见强调色）
private val themeColorOptions = listOf(
    Color(0xFF000000),
    Color(0xFFFFFFFF),
    Color(0xFF1C1C1C),
    Color(0xFF4A4A4A),
    Color(0xFF8A8A8A),
    Color(0xFFE0E0E0),
    Color(0xFF6200EE),
    Color(0xFF3700B3),
    Color(0xFF03DAC5),
    Color(0xFF018786),
    Color(0xFFBB86FC),
    Color(0xFF0D47A1),
    Color(0xFF004D40),
    Color(0xFF33691E),
    Color(0xFFE65100),
    Color(0xFFB71C1C),
    Color(0xFF880E4F),
    Color(0xFF4A148C),
)
