package com.example.securevault.ui.components.richtext

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun FormattingContextMenu(
    visible: Boolean,
    onDismiss: () -> Unit,
    onFormatSelected: (FormatType) -> Unit
) {
    if (!visible) return

    Popup(
        alignment = Alignment.TopCenter,
        properties = PopupProperties(focusable = true),
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FormatButton(
                    icon = Icons.Default.FormatBold,
                    label = "B",
                    onClick = {
                        onFormatSelected(FormatType.BOLD)
                        onDismiss()
                    }
                )
                FormatButton(
                    icon = Icons.Default.FormatItalic,
                    label = "I",
                    onClick = {
                        onFormatSelected(FormatType.ITALIC)
                        onDismiss()
                    }
                )
                FormatButton(
                    icon = Icons.Default.FormatStrikethrough,
                    label = "S",
                    onClick = {
                        onFormatSelected(FormatType.STRIKETHROUGH)
                        onDismiss()
                    }
                )
                FormatButton(
                    icon = Icons.Default.Code,
                    label = "C",
                    onClick = {
                        onFormatSelected(FormatType.CODE)
                        onDismiss()
                    }
                )
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .padding(vertical = 4.dp)
                )
                FormatButton(
                    icon = Icons.Default.FormatListBulleted,
                    label = "•",
                    onClick = {
                        onFormatSelected(FormatType.BULLET_LIST)
                        onDismiss()
                    }
                )
                FormatButton(
                    icon = Icons.Default.FormatListNumbered,
                    label = "1.",
                    onClick = {
                        onFormatSelected(FormatType.NUMBERED_LIST)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun FormatButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

enum class FormatType {
    BOLD,
    ITALIC,
    STRIKETHROUGH,
    CODE,
    BULLET_LIST,
    NUMBERED_LIST
}

/**
 * Applies formatting to the given text based on the format type
 */
fun applyFormatting(
    text: String,
    selectionStart: Int,
    selectionEnd: Int,
    formatType: FormatType
): Pair<String, Int> {
    val before = text.substring(0, selectionStart)
    val selected = text.substring(selectionStart, selectionEnd)
    val after = text.substring(selectionEnd)

    return when (formatType) {
        FormatType.BOLD -> {
            val newText = "$before*$selected*$after"
            val newCursor = if (selected.isEmpty()) selectionStart + 1 else selectionEnd + 2
            newText to newCursor
        }
        FormatType.ITALIC -> {
            val newText = "$before _${selected}_ $after"
            val newCursor = if (selected.isEmpty()) selectionStart + 1 else selectionEnd + 2
            newText to newCursor
        }
        FormatType.STRIKETHROUGH -> {
            val newText = "$before~$selected~$after"
            val newCursor = if (selected.isEmpty()) selectionStart + 1 else selectionEnd + 2
            newText to newCursor
        }
        FormatType.CODE -> {
            val newText = "$before`$selected`$after"
            val newCursor = if (selected.isEmpty()) selectionStart + 1 else selectionEnd + 2
            newText to newCursor
        }
        FormatType.BULLET_LIST -> {
            // Find the start of the current line
            val lineStart = text.lastIndexOf('\n', selectionStart - 1) + 1
            val newText = text.substring(0, lineStart) + "- " + text.substring(lineStart)
            val newCursor = selectionStart + 2
            newText to newCursor
        }
        FormatType.NUMBERED_LIST -> {
            // Find the start of the current line
            val lineStart = text.lastIndexOf('\n', selectionStart - 1) + 1
            val newText = text.substring(0, lineStart) + "1. " + text.substring(lineStart)
            val newCursor = selectionStart + 3
            newText to newCursor
        }
    }
}
