package com.example.securevault.ui.components.richtext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

/**
 * Parses markdown-style text and returns formatted AnnotatedString
 * Supports: *bold*, _italic_, ~strikethrough~, `code`, bullet lists, numbered lists
 */
object RichTextParser {

    data class ParseResult(
        val annotatedString: AnnotatedString,
        val originalLength: Int
    )

    fun parse(text: String): AnnotatedString {
        if (text.isEmpty()) return AnnotatedString("")

        val lines = text.split("\n")
        return buildAnnotatedString {
            val numberCounters = mutableMapOf<Int, Int>()

            lines.forEachIndexed { index, line ->
                if (index > 0) append("\n")

                val level = getNestingLevel(line)
                val trimmed = line.trimStart()

                when {
                    // Bullet list
                    trimmed.startsWith("- ") -> {
                        val indent = "  ".repeat(level)
                        val symbol = getBulletSymbol(level)
                        val content = trimmed.substring(2)
                        
                        append(indent)
                        append(symbol)
                        append(" ")
                        appendInlineFormatting(content)
                    }
                    // Numbered list
                    trimmed.matches(Regex("""^\d+\.\s+.*""")) -> {
                        val number = numberCounters.getOrPut(level) { 0 } + 1
                        numberCounters[level] = number
                        // Reset deeper levels
                        numberCounters.keys.filter { it > level }.forEach { numberCounters.remove(it) }

                        val indent = "  ".repeat(level)
                        val content = trimmed.substring(trimmed.indexOf(". ") + 2)
                        
                        append(indent)
                        append("$number. ")
                        appendInlineFormatting(content)
                    }
                    // Plain text
                    else -> {
                        // Reset number counters for non-list lines
                        numberCounters.clear()
                        appendInlineFormatting(line)
                    }
                }
            }
        }
    }

    private fun AnnotatedString.Builder.appendInlineFormatting(text: String) {
        var currentIndex = 0
        val patterns = listOf(
            FormattingPattern(Regex("""\*([^*]+)\*"""), SpanStyle(fontWeight = FontWeight.Bold)),
            FormattingPattern(Regex("""_([^_]+)_"""), SpanStyle(fontStyle = FontStyle.Italic)),
            FormattingPattern(Regex("""~([^~]+)~"""), SpanStyle(textDecoration = TextDecoration.LineThrough)),
            FormattingPattern(Regex("""`([^`]+)`"""), SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0xFFEEEEEE)))
        )

        // Find all matches across all patterns
        val allMatches = mutableListOf<Match>()
        patterns.forEach { pattern ->
            pattern.regex.findAll(text).forEach { matchResult ->
                allMatches.add(Match(matchResult.range, matchResult.groupValues[1], pattern.style))
            }
        }

        // Sort by start position
        allMatches.sortBy { it.range.first }

        // Build string with formatting
        allMatches.forEach { match ->
            // Append text before match
            if (currentIndex < match.range.first) {
                append(text.substring(currentIndex, match.range.first))
            }

            // Append formatted content (without symbols)
            val start = length
            append(match.content)
            addStyle(match.style, start, length)

            currentIndex = match.range.last + 1
        }

        // Append remaining text
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }

    private fun getNestingLevel(line: String): Int {
        val leadingSpaces = line.takeWhile { it == ' ' }.length
        return (leadingSpaces / 2).coerceIn(0, 2) // Max 3 levels (0, 1, 2)
    }

    private fun getBulletSymbol(level: Int): String = when (level) {
        0 -> "•"  // Bullet
        1 -> "◦"  // White bullet
        2 -> "▪"  // Small square
        else -> "•"
    }

    private data class FormattingPattern(
        val regex: Regex,
        val style: SpanStyle
    )

    private data class Match(
        val range: IntRange,
        val content: String,
        val style: SpanStyle
    )
}
