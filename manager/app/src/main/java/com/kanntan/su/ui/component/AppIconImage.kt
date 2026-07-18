package com.kanntan.su.ui.component

import android.content.pm.PackageInfo
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap

/**
 * App Icon Image Component - Displays app icon with caching
 * 
 * @param packageInfo Package info to load icon from
 * @param label App label for content description
 * @param modifier Modifier for layout
 * @param size Size in dp, default 48dp
 */
@Composable
fun AppIconImage(
    packageInfo: PackageInfo,
    label: String,
    modifier: Modifier = Modifier,
    size: Int = 48
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    LaunchedEffect(packageInfo.packageName) {
        try {
            val icon = packageInfo.applicationInfo?.loadIcon(context.packageManager)
            icon?.toBitmap()?.let { bmp ->
                bitmap = bmp.asImageBitmap()
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    Crossfade(
        targetState = bitmap,
        label = "AppIconCrossfade",
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 150)
    ) { bmp ->
        if (bmp != null) {
            Image(
                bitmap = bmp,
                contentDescription = label,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder
            androidx.compose.foundation.layout.Box(modifier = modifier)
        }
    }
}