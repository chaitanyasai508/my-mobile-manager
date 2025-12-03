package com.example.securevault.ui.components.richtext

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Inserts formatting at cursor position or wraps selected text
 */
fun insertFormatting(
    currentValue: TextFieldValue,
    formatType: FormatType
): TextFieldValue {
    val text = currentValue.text
    val selection = currentValue.selection
    val hasSelection = selection.start != selection.end

    return when (formatType) {
        FormatType.BOLD -> {
            if (hasSelection) {
                wrapSelection(currentValue, "*", "*")
            } else {
                insertSymbols(currentValue, "*", "*")
            }
        }
        FormatType.ITALIC -> {
            if (hasSelection) {
                wrapSelection(currentValue, "_", "_")
            } else {
                insertSymbols(currentValue, "_", "_")
            }
        }
        FormatType.STRIKETHROUGH -> {
            if (hasSelection) {
                wrapSelection(currentValue, "~", "~")
            } else {
                insertSymbols(currentValue, "~", "~")
            }
        }
        FormatType.BULLET_LIST -> {
            insertListPrefix(currentValue, "- ")
        }
        FormatType.NUMBERED_LIST -> {
            insertListPrefix(currentValue, "1. ")
        }
    }
}

/**
 * Wraps selected text with prefix and suffix
 */
private fun wrapSelection(
    value: TextFieldValue,
    prefix: String,
    suffix: String
): TextFieldValue {
    val text = value.text
    val selection = value.selection

    val before = text.substring(0, selection.start)
    val selected = text.substring(selection.start, selection.end)
    val after = text.substring(selection.end)

    val newText = "$before$prefix$selected$suffix$after"
    val newCursor = selection.end + prefix.length + suffix.length

    return TextFieldValue(
        text = newText,
        selection = TextRange(newCursor)
    )
}

/**
 * Inserts symbols with cursor in the middle
 */
private fun insertSymbols(
    value: TextFieldValue,
    prefix: String,
    suffix: String
): TextFieldValue {
    val text = value.text
    val cursor = value.selection.start

    val before = text.substring(0, cursor)
    val after = text.substring(cursor)

    val newText = "$before$prefix$suffix$after"
    val newCursor = cursor + prefix.length

    return TextFieldValue(
        text = newText,
        selection = TextRange(newCursor)
    )
}

/**
 * Inserts list prefix at the start of current line
 */
private fun insertListPrefix(
    value: TextFieldValue,
    prefix: String
): TextFieldValue {
    val text = value.text
    val cursor = value.selection.start

    // Find start of current line
    val lineStart = text.lastIndexOf('\n', cursor - 1) + 1

    val before = text.substring(0, lineStart)
    val after = text.substring(lineStart)

    val newText = "$before$prefix$after"
    val newCursor = cursor + prefix.length

    return TextFieldValue(
        text = newText,
        selection = TextRange(newCursor)
    )
}

/**
 * Handles Enter key with smart list continuation
 */
fun handleEnterKey(value: TextFieldValue): TextFieldValue? {
    val text = value.text
    val cursor = value.selection.start

    // Find current line
    val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
    val lineEnd = text.indexOf('\n', cursor).let { if (it == -1) text.length else it }
    val currentLine = text.substring(lineStart, lineEnd)
    val trimmedLine = currentLine.trimStart()

    // Check if we're in a list
    val isBulletList = trimmedLine.startsWith("- ")
    val isNumberedList = trimmedLine.matches(Regex("""^\d+\.\s+.*"""))

    if (!isBulletList && !isNumberedList) {
        return null // Let default Enter behavior happen
    }

    // Get indentation
    val indentation = currentLine.takeWhile { it == ' ' }

    // Check if list item is empty
    val contentAfterPrefix = if (isBulletList) {
        trimmedLine.substring(2).trim()
    } else {
        trimmedLine.substring(trimmedLine.indexOf(". ") + 2).trim()
    }

    if (contentAfterPrefix.isEmpty()) {
        // Exit list mode - remove prefix and add newline
        val before = text.substring(0, lineStart)
        val after = text.substring(lineEnd)
        val newText = "$before$indentation\n$after"
        
        return TextFieldValue(
            text = newText,
            selection = TextRange(lineStart + indentation.length + 1)
        )
    }

    // Continue list
    val prefix = if (isBulletList) "- " else "1. "
    val before = text.substring(0, cursor)
    val after = text.substring(cursor)
    val newText = "$before\n$indentation$prefix$after"
    val newCursor = cursor + 1 + indentation.length + prefix.length

    return TextFieldValue(
        text = newText,
        selection = TextRange(newCursor)
    )
}
