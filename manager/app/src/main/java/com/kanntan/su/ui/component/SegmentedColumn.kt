package com.kanntan.su.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kanntan.su.ui.theme.PureWhite
import com.kanntan.su.ui.theme.TextOnWhite

/**
 * MD2 Style SegmentedColumn - Groups related items together
 */
@Composable
fun SegmentedColumn(
    modifier: Modifier = Modifier,
    title: String = "",
    content: List<@Composable () -> Unit>,
) {
    if (content.isEmpty()) return

    Column(modifier = modifier) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }
        Column {
            content.forEach { itemContent ->
                itemContent()
            }
        }
    }
}

/**
 * MD2 Style SegmentedListItem - Clickable list item
 */
@Composable
fun SegmentedListItem(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PureWhite)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingContent?.invoke()
        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            headlineContent()
            supportingContent?.invoke()
        }
        trailingContent?.invoke()
    }
}

/**
 * MD2 Style SegmentedSwitchItem - List item with switch
 */
@Composable
fun SegmentedSwitchItem(
    icon: ImageVector? = null,
    title: String,
    summary: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    SegmentedListItem(
        onClick = { if (enabled) onCheckedChange(!checked) },
        enabled = enabled,
        headlineContent = { Text(title, color = TextOnWhite) },
        leadingContent = icon?.let { { Icon(it, title, tint = TextOnWhite) } },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        },
        supportingContent = summary?.let { { Text(summary, color = TextOnWhite.copy(alpha = 0.7f)) } }
    )
}

/**
 * MD2 Style SegmentedDropdownItem - List item with dropdown
 */
@Composable
fun SegmentedDropdownItem(
    icon: ImageVector? = null,
    title: String,
    summary: String? = null,
    items: List<String>,
    enabled: Boolean = true,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    SegmentedListItem(
        onClick = if (enabled) { { expanded = true } } else null,
        enabled = enabled,
        headlineContent = { Text(title, color = TextOnWhite) },
        leadingContent = icon?.let { { Icon(it, title, tint = TextOnWhite) } },
        trailingContent = {
            Box {
                Text(
                    text = if (items.isNotEmpty() && selectedIndex >= 0) items[selectedIndex] else "",
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(0.3f),
                    color = TextOnWhite
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    items.forEachIndexed { index, text ->
                        DropdownMenuItem(
                            text = { Text(text, color = TextOnWhite) },
                            onClick = {
                                onItemSelected(index)
                                expanded = false
                            }
                        )
                    }
                }
            }
        },
        supportingContent = summary?.let { { Text(summary, color = TextOnWhite.copy(alpha = 0.7f)) } }
    )
}

/**
 * MD2 Style SegmentedRadioItem - Radio button list item
 */
@Composable
fun SegmentedRadioItem(
    title: String,
    summary: String? = null,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    SegmentedListItem(
        onClick = onClick,
        enabled = enabled,
        headlineContent = { Text(title, color = TextOnWhite) },
        leadingContent = {
            RadioButton(
                selected = selected,
                onClick = onClick,
                enabled = enabled
            )
        },
        supportingContent = summary?.let { { Text(summary, color = TextOnWhite.copy(alpha = 0.7f)) } }
    )
}

/**
 * MD2 Style SegmentedCheckboxItem - Checkbox list item
 */
@Composable
fun SegmentedCheckboxItem(
    title: String,
    summary: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    SegmentedListItem(
        onClick = { if (enabled) onCheckedChange(!checked) },
        enabled = enabled,
        headlineContent = { Text(title, color = TextOnWhite) },
        leadingContent = {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        },
        supportingContent = summary?.let { { Text(summary, color = TextOnWhite.copy(alpha = 0.7f)) } }
    )
}