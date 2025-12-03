package com.example.securevault.ui.components.richtext

/**
 * Utility functions for rich text editing
 */
object RichTextUtils {

    /**
     * Handles Enter key press with smart list continuation
     * Returns the new text and cursor position
     */
    fun handleEnterKey(
        text: String,
        cursorPosition: Int
    ): Pair<String, Int> {
        // Find the current line
        val lineStart = text.lastIndexOf('\n', cursorPosition - 1) + 1
        val lineEnd = text.indexOf('\n', cursorPosition).let { if (it == -1) text.length else it }
        val currentLine = text.substring(lineStart, lineEnd)
        val trimmedLine = currentLine.trimStart()

        // Check if we're in a list
        val isBulletList = trimmedLine.startsWith("- ")
        val isNumberedList = trimmedLine.matches(Regex("""^\d+\.\s+.*"""))

        if (!isBulletList && !isNumberedList) {
            // Normal enter
            val newText = text.substring(0, cursorPosition) + "\n" + text.substring(cursorPosition)
            return newText to (cursorPosition + 1)
        }

        // Get indentation (leading spaces)
        val indentation = currentLine.takeWhile { it == ' ' }

        // Check if the list item is empty (only has the prefix)
        val contentAfterPrefix = if (isBulletList) {
            trimmedLine.substring(2).trim()
        } else {
            trimmedLine.substring(trimmedLine.indexOf(". ") + 2).trim()
        }

        if (contentAfterPrefix.isEmpty()) {
            // Empty list item - exit list mode
            // Remove the list prefix from current line
            val before = text.substring(0, lineStart)
            val after = text.substring(lineEnd)
            val newText = before + indentation + "\n" + after
            return newText to (lineStart + indentation.length + 1)
        }

        // Continue the list
        val prefix = if (isBulletList) "- " else "1. "
        val newText = text.substring(0, cursorPosition) + "\n$indentation$prefix" + text.substring(cursorPosition)
        val newCursor = cursorPosition + 1 + indentation.length + prefix.length

        return newText to newCursor
    }

    /**
     * Increases indentation (adds 2 spaces at line start)
     */
    fun increaseIndentation(
        text: String,
        cursorPosition: Int
    ): Pair<String, Int> {
        val lineStart = text.lastIndexOf('\n', cursorPosition - 1) + 1
        val currentLine = text.substring(lineStart, cursorPosition.coerceAtMost(text.length))
        val leadingSpaces = currentLine.takeWhile { it == ' ' }.length

        // Max 3 levels (0, 2, 4 spaces)
        if (leadingSpaces >= 4) {
            return text to cursorPosition
        }

        val newText = text.substring(0, lineStart) + "  " + text.substring(lineStart)
        return newText to (cursorPosition + 2)
    }

    /**
     * Decreases indentation (removes 2 spaces from line start)
     */
    fun decreaseIndentation(
        text: String,
        cursorPosition: Int
    ): Pair<String, Int> {
        val lineStart = text.lastIndexOf('\n', cursorPosition - 1) + 1
        val currentLine = text.substring(lineStart, cursorPosition.coerceAtMost(text.length))
        val leadingSpaces = currentLine.takeWhile { it == ' ' }.length

        if (leadingSpaces < 2) {
            return text to cursorPosition
        }

        val newText = text.substring(0, lineStart) + text.substring(lineStart + 2)
        return newText to (cursorPosition - 2).coerceAtLeast(lineStart)
    }

    /**
     * Strips all formatting symbols for plain text preview
     */
    fun stripFormatting(text: String): String {
        return text
            .replace(Regex("""\*([^*]+)\*"""), "$1")  // Bold
            .replace(Regex("""_([^_]+)_"""), "$1")     // Italic
            .replace(Regex("""~([^~]+)~"""), "$1")     // Strikethrough
            .replace(Regex("""`([^`]+)`"""), "$1")     // Code
            .replace(Regex("""^(\s*)- """, RegexOption.MULTILINE), "$1• ") // Bullet
            .replace(Regex("""^(\s*)\d+\. """, RegexOption.MULTILINE), "$1") // Numbered
    }
}
