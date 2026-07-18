package com.kanntan.su.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kanntan.su.ui.theme.ContentBackground
import com.kanntan.su.ui.theme.PureBlack
import com.kanntan.su.ui.theme.PureWhite
import com.kanntan.su.ui.theme.TextOnBlack
import com.kanntan.su.ui.theme.TextOnWhite

// 固定尺寸常量，确保不同区域使用一致的块尺寸以便精确对齐
private val SMALL_BLOCK_DP = 48.dp
private val WHITE_SQUARE_SIZE_DP = 96.dp
private val FOOTER_CANVAS_SIZE_DP = 160.dp
private val FOOTER_BIG_BLOCK_SIZE_DP = 112.dp
private val HEADER_HEIGHT_DP = 280.dp

/**
 * KanntanSU Home Screen
 * 
 * 修复要求：
 * - 三行文字紧凑排列(line-height 1.0)
 * - 白块在底层不遮挡文字
 * - 硬派阶梯渐变
 * - 底部padding防截断
 * - 右下角黑块精准对齐
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    actions: HomeActions = HomeActions(),
    wallpaperBitmap: android.graphics.Bitmap? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) { viewModel.refresh() }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ContentBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Area
            HeaderArea(
                ksuStatusMessage = uiState.getKSUStatusMessage(),
                onHeaderClick = {
                    if (uiState.systemInfo.selinuxStatus == "Permissive") {
                        actions.onJailbreakClick()
                    } else {
                        actions.onInstallClick()
                    }
                }
            )
            
            // Gradient Strip
            HeaderGradientStrip()
            
            // Content Area - 增加底部padding
            ContentArea(
                systemInfo = uiState.systemInfo,
                modifier = Modifier.weight(1f)
            )
            
            // Footer Area - 右下角精准对齐黑块
            FooterArea(
                visible = uiState.menuState != HomeUiState.MenuState.HIDDEN,
                onMenuToggle = {
                    viewModel.toggleMenu()
                    actions.onMenuToggle()
                }
            )
        }
        
        // Floating Menu - 绝对定位在Footer上方
        FloatingMenu(
            visible = uiState.menuState != HomeUiState.MenuState.HIDDEN,
            selectedItem = uiState.selectedMenuItem,
            onModuleClick = {
                actions.onModulesClick()
            },
            onApplicationClick = {
                actions.onApplicationClick()
            },
            onSettingClick = {
                actions.onSettingClick()
            }
        )
    }
}

// ============================================================================
// HEADER AREA - 白块底层，文字上层，三行紧凑
// ============================================================================

@Composable
private fun HeaderArea(
    ksuStatusMessage: String,
    onHeaderClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HEADER_HEIGHT_DP)
            .background(PureBlack)
    ) {
        // 白块 - 在底层(left=0, top约40%高度)
        // 使用常量尺寸确保与 Footer 的小黑块一致，从而能在不同区域实现精确对齐
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val headerHeightPx = HEADER_HEIGHT_DP.toPx()
            val whiteSquareSize = WHITE_SQUARE_SIZE_DP.toPx()

            // 绝对定位：白块位置以 header 常量高度为基准，左上角锚点
            val whiteSquareLeft = 0f
            val whiteSquareTop = headerHeightPx * 0.18f

            // 绘制大白色方块（在文字下方）
            drawRect(
                color = PureWhite,
                topLeft = Offset(whiteSquareLeft, whiteSquareTop),
                size = Size(whiteSquareSize, whiteSquareSize)
            )

            // 交叉交界的小白块：仍保持部分外溢，但同时绘制一个紧邻大白块右下角的"接合"小白块，
            // 使两者角对角完美对齐（小白块的 topLeft 恰等于大白块的 bottomRight）。
            val innerCrossSize = SMALL_BLOCK_DP.toPx()

            // 小白块：只保留与大白块角对角精确对齐的小白块（topLeft = 大白块的 bottomRight）
            val joinLeft = whiteSquareLeft + whiteSquareSize
            val joinTop = whiteSquareTop + whiteSquareSize
            drawRect(
                color = PureWhite,
                topLeft = Offset(joinLeft, joinTop),
                size = Size(innerCrossSize, innerCrossSize)
            )
        }
        
        // 文字 - 在上层，保证位于白块右侧
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onHeaderClick)
                .padding(start = WHITE_SQUARE_SIZE_DP + 12.dp, top = HEADER_HEIGHT_DP * 0.18f)
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                // 第一行：KernelSU - 48sp ExtraBold, line-height 1.0
                Text(
                    text = "KernelSU",
                    color = TextOnBlack,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 44.sp
                )
                
                // 第二行：is - 24sp Normal, 紧凑
                Text(
                    text = "is",
                    color = TextOnBlack,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    modifier = Modifier.offset(y = (-10).dp)
                )
                
                // 第三行：状态文字 - 根据SELinux状态显示不同内容
                Text(
                    text = ksuStatusMessage,
                    color = TextOnBlack,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 28.sp,
                    modifier = Modifier.offset(x = 0.dp, y = (-14).dp)
                )
            }
        }
    }
}

// ============================================================================
// GRADIENT STRIP - 硬派阶梯渐变（扫描线效果）
// ============================================================================

@Composable
private fun HeaderGradientStrip() {
    // 硬派渐变：用24条极细横线模拟更连贯的扫描线效果
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        // 24条从黑到灰到白的渐变横条，每条2dp
        val strips = listOf(
            Color(0xFF000000),
            Color(0xFF080808),
            Color(0xFF101010),
            Color(0xFF181818),
            Color(0xFF202020),
            Color(0xFF282828),
            Color(0xFF303030),
            Color(0xFF383838),
            Color(0xFF404040),
            Color(0xFF484848),
            Color(0xFF505050),
            Color(0xFF585858),
            Color(0xFF606060),
            Color(0xFF686868),
            Color(0xFF707070),
            Color(0xFF787878),
            Color(0xFF808080),
            Color(0xFF888888),
            Color(0xFF909090),
            Color(0xFF989898),
            Color(0xFFA0A0A0),
            Color(0xFFB0B0B0),
            Color(0xFFC8C8C8),
            Color(0xFFE0E0E0),
            Color(0xFFF0F0F0)
        )

        strips.forEach { c ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(c)
            )
        }
    }
}

// ============================================================================
// CONTENT AREA - 底部padding防截断，间距均匀
// ============================================================================

@Composable
private fun ContentArea(
    systemInfo: SystemInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PureWhite)
            .verticalScroll(rememberScrollState())
            .padding(end = 24.dp, top = 32.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.End
    ) {
        InfoRowRightAligned(
            label = "内核版本",
            value = systemInfo.kernelVersion
        )

        Spacer(modifier = Modifier.height(24.dp))

        InfoRowRightAligned(
            label = "管理器版本",
            value = systemInfo.managerVersion
        )

        Spacer(modifier = Modifier.height(24.dp))

        InfoRowRightAligned(
            label = "系统指纹",
            value = systemInfo.fingerprint,
            isMultiLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        InfoRowRightAligned(
            label = "SELinux状态",
            value = systemInfo.selinuxStatus
        )
    }
}

@Composable
private fun InfoRowRightAligned(
    label: String,
    value: String,
    isMultiLine: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = label,
            color = TextOnWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            color = TextOnWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            maxLines = if (isMultiLine) Int.MAX_VALUE else 1,
            textAlign = TextAlign.End
        )
    }
}

// ============================================================================
// FOOTER AREA - 右下角精准对齐黑块，可拖动交互
// ============================================================================

@Composable
private fun FooterArea(
    visible: Boolean,
    onMenuToggle: () -> Unit
) {
    val density = LocalDensity.current
    
    // 小黑块状态：初始位置 (0, 0)
    var smallBlockOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var isMoved by remember { mutableStateOf(false) }
    
    // 动画位置
    val animatedSmallX by animateFloatAsState(
        targetValue = smallBlockOffset.x,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "smallBlockX"
    )
    val animatedSmallY by animateFloatAsState(
        targetValue = smallBlockOffset.y,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "smallBlockY"
    )
    
    // 监听菜单收起
    LaunchedEffect(visible) {
        if (!visible && isMoved) {
            isMoved = false
            smallBlockOffset = Offset(0f, 0f)
        }
    }
    
    // 菜单显示/隐藏动画
    val menuVisible by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f),
        label = "menuVisibility"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(FOOTER_CANVAS_SIZE_DP)
    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(FOOTER_CANVAS_SIZE_DP)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // 小黑块中心移动到点击位置，所以 top-left 要偏移半个小黑块尺寸
                        val centerOffsetX = with(density) { SMALL_BLOCK_DP.toPx() / 2 }
                        val centerOffsetY = with(density) { SMALL_BLOCK_DP.toPx() / 2 }
                        smallBlockOffset = Offset(tapOffset.x - centerOffsetX, tapOffset.y - centerOffsetY)
                        isMoved = true
                        onMenuToggle()
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            val bigSize = FOOTER_BIG_BLOCK_SIZE_DP.toPx()
            val smallSizePx = SMALL_BLOCK_DP.toPx()
            
            // 大黑块 - 紧贴右下角
            drawRect(
                color = PureBlack,
                topLeft = Offset(canvasWidth - bigSize, canvasHeight - bigSize),
                size = Size(bigSize, bigSize)
            )
            
            // 小黑块位置（动画值）
            val smallLeft = animatedSmallX
            val smallTop = animatedSmallY
            
            // 先绘制小黑块为黑色
            drawRect(
                color = PureBlack,
                topLeft = Offset(smallLeft, smallTop),
                size = Size(smallSizePx, smallSizePx)
            )
            
            // 计算重叠区域
            val overlapLeft = maxOf(smallLeft, canvasWidth - bigSize)
            val overlapTop = maxOf(smallTop, canvasHeight - bigSize)
            val overlapRight = minOf(smallLeft + smallSizePx, canvasWidth)
            val overlapBottom = minOf(smallTop + smallSizePx, canvasHeight)
            
            // 如果有重叠，在重叠区域绘制白色
            if (overlapLeft < overlapRight && overlapTop < overlapBottom) {
                drawRect(
                    color = PureWhite,
                    topLeft = Offset(overlapLeft, overlapTop),
                    size = Size(overlapRight - overlapLeft, overlapBottom - overlapTop)
                )
            }
        }
    }
}

// ============================================================================
// FLOATING MENU
// ============================================================================

@Composable
private fun FloatingMenu(
    visible: Boolean,
    selectedItem: HomeUiState.MenuItemType?,
    onModuleClick: () -> Unit,
    onApplicationClick: () -> Unit,
    onSettingClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 24.dp, bottom = 180.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        androidx.compose.animation.Crossfade(
            targetState = visible,
            animationSpec = tween(500, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)),
            label = "menuCrossfade"
        ) { show ->
            if (show) {
                Column(
                    modifier = Modifier.width(160.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Modules Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ContentBackground),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = {
                                android.util.Log.d("FloatingMenu", "Modules card clicked")
                                onModuleClick()
                            })
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(modifier = Modifier.size(24.dp)) {
                                drawRect(color = PureBlack, topLeft = Offset.Zero, size = Size(size.width, size.height))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "Modules", color = TextOnWhite, fontSize = 14.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Application Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ContentBackground),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { onApplicationClick() })
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(modifier = Modifier.size(24.dp)) {
                                val path = Path().apply {
                                    moveTo(size.width / 2, 0f)
                                    lineTo(size.width, size.height)
                                    lineTo(0f, size.height)
                                    close()
                                }
                                drawPath(path = path, color = PureBlack)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "Application", color = TextOnWhite, fontSize = 14.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Setting Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ContentBackground),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { onSettingClick() })
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(modifier = Modifier.size(24.dp)) {
                                drawCircle(color = PureBlack, radius = size.minDimension / 2)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "Setting", color = TextOnWhite, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(1.dp))
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            drawRect(
                color = PureBlack,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = label,
            color = TextOnWhite,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        viewModel = remember { HomeViewModel() },
        actions = HomeActions()
    )
}
