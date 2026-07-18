package com.kanntan.su.ui.screen.colorpalette

import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanntan.su.ui.theme.ContentBackground
import com.kanntan.su.ui.theme.PureBlack
import com.kanntan.su.ui.theme.PureWhite
import com.kanntan.su.ui.theme.TextOnBlack
import com.kanntan.su.ui.theme.TextOnWhite

/**
 * Color Palette Screen - 主题自定义页面
 * 支持自定义背景色、强调色、主页大按钮颜色
 */
@Composable
fun ColorPaletteScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE) }
    
    var backgroundColor by remember { mutableStateOf(prefs.getInt("background_color", PureWhite.hashCode().toInt())) }
    var accentColor by remember { mutableStateOf(prefs.getInt("accent_color", PureBlack.hashCode().toInt())) }
    var headerColor by remember { mutableStateOf(prefs.getInt("header_color", PureBlack.hashCode().toInt())) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ContentBackground)
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
            // Background Color Section
            ColorSection(
                title = "应用背景",
                colors = backgroundColorOptions,
                selectedColor = backgroundColor,
                onColorSelected = { color ->
                    backgroundColor = color.hashCode().toInt()
                    prefs.edit().putInt("background_color", color.hashCode().toInt()).apply()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Header Color Section
            ColorSection(
                title = "主页上半部分大按钮",
                colors = headerColorOptions,
                selectedColor = headerColor,
                onColorSelected = { color ->
                    headerColor = color.hashCode().toInt()
                    prefs.edit().putInt("header_color", color.hashCode().toInt()).apply()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Accent Color Section
            ColorSection(
                title = "应用组件强调色",
                colors = accentColorOptions,
                selectedColor = accentColor,
                onColorSelected = { color ->
                    accentColor = color.hashCode().toInt()
                    prefs.edit().putInt("accent_color", color.hashCode().toInt()).apply()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Preview Section
            Text(
                text = "预览",
                color = TextOnWhite.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(backgroundColor)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(Color(headerColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "KernelSU",
                            color = PureWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Button preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color(accentColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "示例按钮",
                            color = PureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPaletteHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PureBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(PureWhite)
                .clickable(onClick = onNavigateBack),
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
            text = "自定义主题",
            color = TextOnBlack,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ColorSection(
    title: String,
    colors: List<Color>,
    selectedColor: Int,
    onColorSelected: (Color) -> Unit
) {
    Column {
        Text(
            text = title,
            color = TextOnWhite.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
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
                                width = if (color.hashCode().toInt() == selectedColor) 3.dp else 0.dp,
                                color = if (color.hashCode().toInt() == selectedColor) PureBlack else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(color) }
                    )
                }
            }
        }
    }
}

// 预定义颜色选项
private val backgroundColorOptions = listOf(
    PureWhite,
    Color(0xFFF5F5F5),
    Color(0xFFE0E0E0),
    Color(0xFF212121),
    PureBlack,
    Color(0xFF1A237E),
    Color(0xFF004D40),
    Color(0xFF880E4F),
    Color(0xFFE65100),
    Color(0xFF33691E),
    Color(0xFF4A148C),
    Color(0xFF0D47A1),
)

private val headerColorOptions = listOf(
    PureBlack,
    PureWhite,
    Color(0xFF212121),
    Color(0xFF1A237E),
    Color(0xFF004D40),
    Color(0xFF880E4F),
    Color(0xFFE65100),
    Color(0xFF33691E),
    Color(0xFF4A148C),
    Color(0xFF0D47A1),
    Color(0xFFB71C1C),
    Color(0xFF1B5E20),
)

private val accentColorOptions = listOf(
    PureBlack,
    PureWhite,
    Color(0xFF6200EE),
    Color(0xFF03DAC5),
    Color(0xFFBB86FC),
    Color(0xFF018786),
    Color(0xFFB00020),
    Color(0xFF000000),
    Color(0xFF3700B3),
    Color(0xFF03DAC6),
    Color(0xFFCF6679),
    Color(0xFF121212),
)
