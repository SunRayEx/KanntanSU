package com.kanntan.su.ui.screen.superuser

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanntan.su.ui.component.AppIconImage
import com.kanntan.su.ui.theme.ContentBackground
import com.kanntan.su.ui.theme.PureBlack
import com.kanntan.su.ui.theme.PureWhite
import com.kanntan.su.ui.theme.TextOnBlack
import com.kanntan.su.ui.theme.TextOnWhite
import me.weishu.kernelsu.data.model.AppInfo
import me.weishu.kernelsu.ui.screen.superuser.GroupedApps
import me.weishu.kernelsu.ui.screen.superuser.SuperUserActions
import me.weishu.kernelsu.ui.screen.superuser.SuperUserUiState
import me.weishu.kernelsu.ui.viewmodel.SuperUserViewModel

private enum class ProfileType {
    DEFAULT,
    TEMPLATE,
    CUSTOM
}

private fun deriveProfileType(group: GroupedApps): ProfileType {
    val profile = group.primary.profile ?: return ProfileType.DEFAULT
    if (!profile.allowSu) return ProfileType.DEFAULT
    return if (profile.rootUseDefault) {
        ProfileType.DEFAULT
    } else if (profile.rootTemplate != null) {
        ProfileType.TEMPLATE
    } else {
        ProfileType.CUSTOM
    }
}

@Composable
fun SuperUserScreen(
    viewModel: SuperUserViewModel,
    actions: SuperUserActions,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initializePreferences()
        viewModel.loadAppList()
    }

    Column(modifier = Modifier.fillMaxSize().background(ContentBackground)) {
        SuperUserHeader(
            onNavigateBack = onNavigateBack,
            onOpenSulog = actions.onOpenSulog
        )

        if (uiState.groupedApps.isEmpty() && !uiState.isRefreshing) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No Apps", color = TextOnWhite, fontSize = 18.sp)
            }
        } else if (uiState.isRefreshing && uiState.groupedApps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PureBlack, strokeWidth = 3.dp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.groupedApps, key = { it.uid }) { group ->
                    SuperUserAppItem(
                        group = group,
                        profileType = deriveProfileType(group),
                        onClick = { actions.onOpenProfile(group) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SuperUserHeader(onNavigateBack: () -> Unit, onOpenSulog: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PureBlack)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                text = "Application",
                color = TextOnBlack,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(PureWhite)
                .clickable(onClick = onOpenSulog),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "≡",
                color = TextOnBlack,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SuperUserAppItem(
    group: GroupedApps,
    profileType: ProfileType,
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconImage(
                    packageInfo = group.primary.packageInfo,
                    label = group.primary.label,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.primary.label,
                        color = TextOnWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = group.primary.packageName,
                        color = TextOnWhite.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(if (group.anyAllowSu) PureBlack else PureWhite)
                        .clickable(onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (group.anyAllowSu) "ON" else "OFF",
                        color = if (group.anyAllowSu) TextOnBlack else TextOnWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(if (group.shouldUmount) PureBlack else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "卸载模块",
                        color = TextOnWhite.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectorButton(
                        label = "默认",
                        isSelected = profileType == ProfileType.DEFAULT,
                        isLocked = false,
                        onClick = { /* TODO: select default */ }
                    )
                    SelectorButton(
                        label = "模板",
                        isSelected = profileType == ProfileType.TEMPLATE,
                        isLocked = false,
                        onClick = { /* TODO: select template */ }
                    )
                    SelectorButton(
                        label = "自定义",
                        isSelected = profileType == ProfileType.CUSTOM,
                        isLocked = false,
                        onClick = { /* TODO: select custom */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectorButton(
    label: String,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(if (isSelected) RoundedCornerShape(2.dp) else CircleShape)
                .background(
                    when {
                        isSelected && isLocked -> Color.Gray
                        isSelected -> PureBlack
                        else -> Color.Gray
                    }
                )
                .clickable(enabled = !isLocked, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Text(
                    text = "✓",
                    color = PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = TextOnWhite.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
    }
}
