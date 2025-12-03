package com.example.securevault.ui.components.richtext

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FormattingToolbar(
    onInsertFormat: (FormatType) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    if (!visible) {
        // Show button to reveal toolbar
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onToggleVisibility) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Show Formatting")
                }
            }
        }
        return
    }

    // Full toolbar
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // Hide button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onToggleVisibility) {
                    Text("Hide", fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Hide toolbar",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Formatting buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ToolbarButton(
                    text = "B",
                    description = "Bold",
                    style = FontWeight.Bold,
                    onClick = { onInsertFormat(FormatType.BOLD) }
                )
                ToolbarButton(
                    text = "I",
                    description = "Italic",
                    fontStyle = FontStyle.Italic,
                    onClick = { onInsertFormat(FormatType.ITALIC) }
                )
                ToolbarButton(
                    text = "S",
                    description = "Strikethrough",
                    textDecoration = TextDecoration.LineThrough,
                    onClick = { onInsertFormat(FormatType.STRIKETHROUGH) }
                )
                ToolbarButton(
                    text = "•",
                    description = "Bullet list",
                    onClick = { onInsertFormat(FormatType.BULLET_LIST) }
                )
                ToolbarButton(
                    text = "1.",
                    description = "Numbered list",
                    onClick = { onInsertFormat(FormatType.NUMBERED_LIST) }
                )
            }

            // Hint text
            Text(
                text = "Tip: Select text and tap a button to format it",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ToolbarButton(
    text: String,
    description: String,
    style: FontWeight? = null,
    fontStyle: FontStyle? = null,
    textDecoration: TextDecoration? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = style,
            fontStyle = fontStyle,
            textDecoration = textDecoration
        )
    }
}

enum class FormatType {
    BOLD,
    ITALIC,
    STRIKETHROUGH,
    BULLET_LIST,
    NUMBERED_LIST
}
