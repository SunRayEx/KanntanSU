package com.kanntan.su.ui.screen.colorpalette

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import com.kanntan.su.ui.theme.DefaultKanntanColors
import com.kanntan.su.ui.theme.LocalKanntanTheme
import com.kanntan.su.ui.theme.decodeSampledBitmap
import com.kanntan.su.ui.theme.kanntanColors
import com.kanntan.su.ui.theme.kanntanImages
import com.kanntan.su.ui.theme.rememberImageAwareForeground
import com.kanntan.su.ui.theme.rememberThemeImage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    val images by theme.images.collectAsState()
    val context = LocalContext.current

    // "image/*" so the picker offers photos and saved images alike.
    val backgroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            copyImageToPrivateStorage(context, uri, "background")
                ?.let(theme::setBackgroundImage)
        }
    }
    val topImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            copyImageToPrivateStorage(context, uri, "top")
                ?.let(theme::setTopImage)
        }
    }

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
            // —— 主页顶部 —— 顶部图片生效后覆盖整个区域，与“主页上半部分”颜色抽屉互斥
            ImageSection(
                title = "主页顶部图片",
                description = "生效后覆盖主页顶部区域，文字会按图片明度自动变深或变浅",
                imagePath = images.topImagePath,
                onPick = { topImageLauncher.launch("image/*") },
                onClear = { theme.setTopImage(null) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (images.topImagePath == null) {
                // 主页上半部分（大按钮区域）— 预设色板 / 自定义图片两个抽屉
                DrawerColorSection(
                    title = "主页上半部分",
                    selectedColor = colors.topColor,
                    onColorSelected = { theme.setTopColor(it) },
                    onClear = { theme.setTopColor(DefaultKanntanColors.topColor) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // —— 主页内容背景 —— 背景图片生效后取代中间区域的纯色背景
            ImageSection(
                title = "主页背景图片",
                description = "生效后覆盖主页中间内容区域，与该分区颜色互斥",
                imagePath = images.backgroundImagePath,
                onPick = { backgroundLauncher.launch("image/*") },
                onClear = { theme.setBackgroundImage(null) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (images.backgroundImagePath == null) {
                // 主页中间内容部分
                DrawerColorSection(
                    title = "主页中间内容部分",
                    selectedColor = colors.middleColor,
                    onColorSelected = { theme.setMiddleColor(it) },
                    onClear = { theme.setMiddleColor(DefaultKanntanColors.middleColor) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 主页下半部分（按钮区域）
            DrawerColorSection(
                title = "主页下半部分",
                selectedColor = colors.bottomColor,
                onColorSelected = { theme.setBottomColor(it) },
                onClear = { theme.setBottomColor(DefaultKanntanColors.bottomColor) }
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
    val images = kanntanImages()
    val topImage = rememberThemeImage(images.topImagePath)
    val backgroundImage = rememberThemeImage(images.backgroundImagePath)
    // Mirror the home page: text over a photo follows the image's luminance.
    val topTextColor = rememberImageAwareForeground(images.topImagePath, colors.onTopColor)
    val middleTextColor = rememberImageAwareForeground(images.backgroundImagePath, colors.onMiddleColor)
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
                    .background(if (topImage == null) colors.topColor else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (topImage != null) {
                    Image(
                        bitmap = topImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    text = "KanntanSU",
                    color = topTextColor,
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
                    .background(if (backgroundImage == null) colors.middleColor else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (backgroundImage != null) {
                    Image(
                        bitmap = backgroundImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    text = "中间内容部分",
                    color = middleTextColor,
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

/**
 * 上/中/下三分区的取色器：两个抽屉。
 *  - 「预设调色板」：Material Design 2 标准色板；
 *  - 「自定义图片」：选一张图，用 Android Palette API 提取其中 8 种主色再选一个。
 *
 * 在任一抽屉里选定颜色后，另一抽屉即隐藏；只有点击当前抽屉的「取消」或页面底部的
 * 「恢复默认」才会重新展开两者。
 */
@Composable
private fun DrawerColorSection(
    title: String,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    onClear: () -> Unit
) {
    val theme = kanntanColors()
    val context = LocalContext.current

    // null = 两个抽屉都显示；选定颜色后锁定为该抽屉，另一抽屉隐藏
    var activeDrawer by remember { mutableStateOf<DrawerKind?>(null) }
    var presetExpanded by remember { mutableStateOf(true) }
    var imageExpanded by remember { mutableStateOf(false) }
    var imagePath by remember { mutableStateOf<String?>(null) }
    var extractedColors by remember { mutableStateOf<List<Color>>(emptyList()) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imagePath = copyImageToPrivateStorage(context, uri, "palette_${title.hashCode()}")
            imageExpanded = true
        }
    }

    // Palette 生成是 CPU 密集型，放到 IO 线程
    LaunchedEffect(imagePath) {
        val path = imagePath
        extractedColors = if (path == null) {
            emptyList()
        } else {
            withContext(Dispatchers.IO) {
                decodeSampledBitmap(context, path)?.let { bitmap ->
                    Palette.from(bitmap).generate()
                        .swatches
                        .sortedByDescending { swatch -> swatch.population }
                        .take(PALETTE_COLOR_COUNT)
                        .map { swatch -> Color(swatch.rgb) }
                } ?: emptyList()
            }
        }
    }

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
            // —— 抽屉一：预设调色板（选了图片色后隐藏）——
            if (activeDrawer != DrawerKind.Image) {
                Drawer(
                    title = "预设调色板",
                    expanded = presetExpanded,
                    onToggle = { presetExpanded = !presetExpanded },
                    showCancel = activeDrawer == DrawerKind.Preset,
                    onCancel = {
                        activeDrawer = null
                        onClear()
                    }
                ) {
                    SwatchGrid(
                        colors = md2StandardPalette,
                        selectedColor = selectedColor,
                        onSelected = {
                            onColorSelected(it)
                            activeDrawer = DrawerKind.Preset
                            presetExpanded = true
                        }
                    )
                }
            }

            // —— 抽屉二：自定义图片（选了预设色后隐藏）——
            if (activeDrawer != DrawerKind.Preset) {
                Drawer(
                    title = "自定义图片",
                    expanded = imageExpanded,
                    onToggle = { imageExpanded = !imageExpanded },
                    showCancel = activeDrawer == DrawerKind.Image,
                    onCancel = {
                        activeDrawer = null
                        onClear()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { imagePicker.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.primaryColor,
                                contentColor = theme.onPrimaryColor
                            )
                        ) {
                            Text(text = "选择图片", fontWeight = FontWeight.Bold)
                        }
                        if (imagePath != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { imagePath = null }) {
                                Text(
                                    text = "移除图片",
                                    color = theme.onSecondaryColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    when {
                        imagePath != null && extractedColors.isEmpty() -> Text(
                            text = "正在提取颜色…",
                            color = theme.onSecondaryColor.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )

                        extractedColors.isNotEmpty() -> SwatchGrid(
                            colors = extractedColors,
                            selectedColor = selectedColor,
                            onSelected = {
                                onColorSelected(it)
                                activeDrawer = DrawerKind.Image
                                imageExpanded = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Drawer(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    showCancel: Boolean,
    onCancel: () -> Unit,
    content: @Composable () -> Unit
) {
    val theme = kanntanColors()
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.Default.KeyboardArrowRight
                    },
                    contentDescription = null,
                    tint = theme.onSecondaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = theme.onSecondaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (showCancel) {
                TextButton(onClick = onCancel) {
                    Text(
                        text = "取消",
                        color = theme.onSecondaryColor.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
        }
        if (expanded) {
            content()
        }
    }
}

/**
 * 非懒加载的色板网格：在 verticalScroll 内嵌套 LazyVerticalGrid 会运行时崩溃。
 */
@Composable
private fun SwatchGrid(
    colors: List<Color>,
    selectedColor: Color,
    onSelected: (Color) -> Unit
) {
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.chunked(PALETTE_COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { color ->
                    Swatch(
                        color = color,
                        isSelected = color == selectedColor,
                        onClick = { onSelected(color) }
                    )
                }
                // pad the last row so short rows stay left-aligned evenly
                repeat(PALETTE_COLUMNS - row.size) {
                    Spacer(modifier = Modifier.size(40.dp))
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
            // Plain grid, not LazyVerticalGrid: this screen is itself inside a
            // verticalScroll column, and nesting a vertically-scrollable lazy grid
            // there throws at runtime ("Nesting scrollable in the same direction").
            SwatchGrid(
                colors = colors,
                selectedColor = selectedColor,
                onSelected = onColorSelected
            )
        }
    }
}

/**
 * Picker row for one customizable theme image.
 *
 * The picked file is copied into app-private storage so it survives reboots
 * without holding a long-lived content-uri permission.
 */
@Composable
private fun ImageSection(
    title: String,
    description: String,
    imagePath: String?,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    val theme = kanntanColors()
    val bitmap = rememberThemeImage(imagePath)

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Preview square: the image once decoded, else the color it would replace.
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (bitmap != null) Color.Transparent else theme.middleColor)
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (imagePath == null) description else "已设置",
                        color = theme.onSecondaryColor.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onPick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.primaryColor,
                                contentColor = theme.onPrimaryColor
                            )
                        ) {
                            Text(text = "选择图片", fontWeight = FontWeight.Bold)
                        }
                        if (imagePath != null) {
                            TextButton(onClick = onClear) {
                                Text(
                                    text = "清除",
                                    color = theme.onSecondaryColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Copy [uri] into app-private storage under [name], returning the absolute path. */
private fun copyImageToPrivateStorage(context: Context, uri: Uri, name: String): String? {
    return runCatching {
        val dir = File(context.filesDir, "theme").apply { mkdirs() }
        val extension = when (context.contentResolver.getType(uri)?.substringAfter('/')) {
            "png" -> "png"
            "webp" -> "webp"
            "gif" -> "gif"
            else -> "jpg"
        }
        val file = File(dir, "$name.$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return@runCatching null
        file.absolutePath
    }.getOrNull()
}

@Composable
private fun Swatch(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    val theme = kanntanColors()
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) theme.primaryColor else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}

// 每个分区抽屉中「自定义图片」最多提取的颜色数
private const val PALETTE_COLOR_COUNT = 8

private enum class DrawerKind { Preset, Image }

// 色板网格的列数
private const val PALETTE_COLUMNS = 6

/** Material Design 2 标准色板：全部色相的 500 色阶 + 黑/白 */
private val md2StandardPalette = listOf(
    Color(0xFFF44336), // Red 500
    Color(0xFFE91E63), // Pink 500
    Color(0xFF9C27B0), // Purple 500
    Color(0xFF673AB7), // Deep Purple 500
    Color(0xFF3F51B5), // Indigo 500
    Color(0xFF2196F3), // Blue 500
    Color(0xFF03A9F4), // Light Blue 500
    Color(0xFF00BCD4), // Cyan 500
    Color(0xFF009688), // Teal 500
    Color(0xFF4CAF50), // Green 500
    Color(0xFF8BC34A), // Light Green 500
    Color(0xFFCDDC39), // Lime 500
    Color(0xFFFFEB3B), // Yellow 500
    Color(0xFFFFC107), // Amber 500
    Color(0xFFFF9800), // Orange 500
    Color(0xFFFF5722), // Deep Orange 500
    Color(0xFF795548), // Brown 500
    Color(0xFF9E9E9E), // Grey 500
    Color(0xFF607D8B), // Blue Grey 500
    Color(0xFF000000), // Black
    Color(0xFFFFFFFF), // White
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
