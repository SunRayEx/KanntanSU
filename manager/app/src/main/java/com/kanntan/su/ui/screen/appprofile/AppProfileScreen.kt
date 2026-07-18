package com.kanntan.su.ui.screen.appprofile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanntan.su.ui.component.SegmentedSwitchItem
import com.kanntan.su.ui.theme.ContentBackground
import com.kanntan.su.ui.theme.PureBlack
import com.kanntan.su.ui.theme.PureWhite
import com.kanntan.su.ui.theme.TextOnBlack
import com.kanntan.su.ui.theme.TextOnWhite
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.profile.Capabilities
import me.weishu.kernelsu.profile.Groups

data class AppProfileActions(
    val onReset: () -> Unit = {},
    val onSave: () -> Unit = {}
)

private enum class ProfileMode {
    DEFAULT, TEMPLATE, CUSTOM
}

@Composable
fun AppProfileScreen(
    packageName: String,
    uid: Int = 0,
    actions: AppProfileActions,
    onNavigateBack: () -> Unit
) {
    var profile by remember { mutableStateOf(Natives.getAppProfile(packageName, uid)) }
    var isLoading by remember { mutableStateOf(false) }

    val initialProfile = remember(packageName, uid) {
        Natives.getAppProfile(packageName, uid)
    }

    val currentMode = when {
        !profile.allowSu -> ProfileMode.DEFAULT
        profile.rootUseDefault -> ProfileMode.DEFAULT
        profile.rootTemplate != null -> ProfileMode.TEMPLATE
        else -> ProfileMode.CUSTOM
    }

    Column(modifier = Modifier.fillMaxSize().background(ContentBackground)) {
        AppProfileHeader(packageName = packageName, onNavigateBack = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AppProfileSection(title = "Root Access") {
                SegmentedSwitchItem(
                    title = "Allow SU",
                    summary = "Grant root access to this app",
                    checked = profile.allowSu,
                    onCheckedChange = { profile = profile.copy(allowSu = it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SegmentedSwitchItem(
                    title = "Umount Modules",
                    summary = "Unmount modules for this app",
                    checked = profile.umountModules,
                    onCheckedChange = { profile = profile.copy(umountModules = it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppProfileSection(title = "Profile Mode") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    ProfileMode.values().forEach { mode ->
                        val isSelected = currentMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(if (isSelected) PureBlack else PureWhite)
                                .clickable {
                                    profile = when (mode) {
                                        ProfileMode.DEFAULT -> profile.copy(
                                            rootUseDefault = true,
                                            rootTemplate = null
                                        )
                                        ProfileMode.TEMPLATE -> profile.copy(
                                            rootUseDefault = false,
                                            rootTemplate = "default"
                                        )
                                        ProfileMode.CUSTOM -> profile.copy(
                                            rootUseDefault = false,
                                            rootTemplate = null
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (mode) {
                                    ProfileMode.DEFAULT -> "默认"
                                    ProfileMode.TEMPLATE -> "模板"
                                    ProfileMode.CUSTOM -> "自定义"
                                },
                                color = if (isSelected) TextOnBlack else TextOnWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (profile.allowSu && currentMode == ProfileMode.CUSTOM) {
                Spacer(modifier = Modifier.height(24.dp))

                AppProfileSection(title = "SELinux Context") {
                    val contexts = listOf("u:r:su:s0", "u:r:untrusted_app:s0", "u:r:shell:s0")
                    contexts.forEach { ctx ->
                        val isChecked = profile.context == ctx
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { profile = profile.copy(context = ctx) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(if (isChecked) PureBlack else PureWhite)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = ctx, color = TextOnWhite, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppProfileSection(title = "Root Profile") {
                    Capabilities.entries.take(10).forEach { cap ->
                        val isChecked = profile.capabilities.contains(cap.cap)
                        SegmentedSwitchItem(
                            title = cap.display,
                            summary = cap.desc,
                            checked = isChecked,
                            onCheckedChange = {
                                val caps = profile.capabilities.toMutableList()
                                if (it) caps.add(cap.cap) else caps.remove(cap.cap)
                                profile = profile.copy(capabilities = caps)
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppProfileSection(title = "Groups") {
                    Groups.entries.take(10).forEach { group ->
                        val isChecked = profile.groups.contains(group.gid)
                        SegmentedSwitchItem(
                            title = group.display,
                            summary = group.desc,
                            checked = isChecked,
                            onCheckedChange = {
                                val grps = profile.groups.toMutableList()
                                if (it) grps.add(group.gid) else grps.remove(group.gid)
                                profile = profile.copy(groups = grps)
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .clickable { profile = initialProfile }
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Reset", color = TextOnWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureBlack),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .clickable {
                            isLoading = true
                            val success = Natives.setAppProfile(profile)
                            isLoading = false
                            if (success) onNavigateBack()
                        }
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        if (isLoading) {
                            CircularProgressIndicator(color = TextOnBlack, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Save", color = TextOnBlack, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppProfileHeader(packageName: String, onNavigateBack: () -> Unit) {
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
            Text("<", color = TextOnBlack, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("App Profile", color = TextOnBlack, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(packageName, color = TextOnBlack.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun AppProfileSection(title: String, content: @Composable () -> Unit) {
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
            Column(modifier = Modifier.padding(8.dp)) { content() }
        }
    }
}
