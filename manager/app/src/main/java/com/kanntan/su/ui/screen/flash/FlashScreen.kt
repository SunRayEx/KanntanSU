package com.kanntan.su.ui.screen.flash

import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanntan.su.ui.theme.ContentBackground
import com.kanntan.su.ui.theme.PureBlack
import com.kanntan.su.ui.theme.TextOnBlack
import com.kanntan.su.ui.theme.TextOnWhite
import com.kanntan.su.ui.util.reboot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.ui.screen.flash.FlashIt
import me.weishu.kernelsu.ui.screen.flash.FlashingStatus
import me.weishu.kernelsu.ui.screen.flash.flashIt
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FlashScreen(
    flashIt: FlashIt,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var text by rememberSaveable { mutableStateOf("") }
    val logContent = rememberSaveable { StringBuilder() }
    var showRebootAction by rememberSaveable { mutableStateOf(false) }
    var flashingStatus by rememberSaveable { mutableStateOf(FlashingStatus.FLASHING) }

    LaunchedEffect(Unit) {
        if (text.isNotEmpty()) return@LaunchedEffect
        var currentText = text
        val mainHandler = Handler(Looper.getMainLooper())
        withContext(Dispatchers.IO) {
            flashIt(flashIt, onStdout = {
                val tempText = "$it\n"
                if (tempText.startsWith("[H[J")) {
                    currentText = tempText.substring(6)
                } else {
                    currentText += tempText
                }
                mainHandler.post {
                    text = currentText
                }
                logContent.append(it).append("\n")
            }, onStderr = {
                logContent.append(it).append("\n")
            }).apply {
                if (code != 0) {
                    currentText += "Error code: $code.\n $err Please save and check the log.\n"
                    mainHandler.post {
                        text = currentText
                    }
                }
                if (showReboot) {
                    currentText += "\n\n\n"
                    mainHandler.post {
                        text = currentText
                        showRebootAction = true
                    }
                }
                mainHandler.post {
                    flashingStatus = if (code == 0) FlashingStatus.SUCCESS else FlashingStatus.FAILED
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        FlashHeader(
            status = flashingStatus,
            onBack = onNavigateBack,
            onSaveLog = {
                scope.launch {
                    val format = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
                    val date = format.format(Date())
                    val file = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "KanntanSU_install_log_${date}.log"
                    )
                    file.writeText(logContent.toString())
                    Toast.makeText(context, "Log saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                }
            }
        )

        FlashContent(
            text = text,
            flashingStatus = flashingStatus,
            showRebootAction = showRebootAction,
            onReboot = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        reboot()
                    }
                }
            }
        )
    }
}

@Composable
private fun FlashHeader(
    status: FlashingStatus,
    onBack: () -> Unit,
    onSaveLog: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PureBlack)
            .padding(top = 40.dp, bottom = 12.dp, start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextOnBlack,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = when (status) {
                FlashingStatus.FLASHING -> "Flashing..."
                FlashingStatus.SUCCESS -> "Flash Success"
                FlashingStatus.FAILED -> "Flash Failed"
            },
            color = TextOnBlack,
            fontSize = 20.sp,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onSaveLog) {
            Icon(
                imageVector = Icons.Filled.Save,
                contentDescription = "Save Log",
                tint = TextOnBlack,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun FlashContent(
    text: String,
    flashingStatus: FlashingStatus,
    showRebootAction: Boolean,
    onReboot: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ContentBackground)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (text.isEmpty() && flashingStatus == FlashingStatus.FLASHING) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                    color = PureBlack
                )
            } else {
                Text(
                    text = text,
                    color = TextOnWhite,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                )
            }

            LaunchedEffect(text) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }

        if (flashingStatus != FlashingStatus.FLASHING) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = when (flashingStatus) {
                                FlashingStatus.SUCCESS -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                FlashingStatus.FAILED -> androidx.compose.ui.graphics.Color(0xFFF44336)
                                else -> PureBlack
                            },
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = when (flashingStatus) {
                        FlashingStatus.SUCCESS -> "Success"
                        FlashingStatus.FAILED -> "Failed"
                        else -> ""
                    },
                    color = TextOnWhite,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                if (showRebootAction) {
                    IconButton(onClick = onReboot) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reboot",
                            tint = PureBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "Reboot",
                        color = PureBlack,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
