package com.example.securevault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.securevault.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    viewModel: MainViewModel,
    noteId: Int? = null,
    onNavigateBack: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val existingNote = notes.find { it.id == noteId }

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    // Use TextFieldValue for cursor position control if needed, but String is simpler for now
    // For rich text editing, we need to manipulate the text at cursor.
    // Let's use TextFieldValue to track selection.
    var content by remember { mutableStateOf(TextFieldValue(existingNote?.content ?: "")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (noteId != null) {
                        IconButton(onClick = {
                            viewModel.deleteNote(noteId)
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(onClick = {
                        if (title.isNotBlank()) {
                            if (noteId == null) {
                                viewModel.addNote(title, content.text)
                            } else {
                                viewModel.updateNote(noteId, title, content.text)
                            }
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Formatting Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = {
                    content = insertFormatting(content, "*", "*")
                }) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Bold")
                }
                IconButton(onClick = {
                    content = insertFormatting(content, "_", "_")
                }) {
                    Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
                }
                IconButton(onClick = {
                    content = insertFormatting(content, "- ", "")
                }) {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = "List")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content Input (Rich Text)
            // We use BasicTextField to have full control over rendering
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                    .padding(4.dp)
            ) {
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    visualTransformation = { text ->
                        // Render markdown symbols
                        val annotatedString = renderMarkdown(text.text)
                        androidx.compose.ui.text.input.TransformedText(annotatedString, androidx.compose.ui.text.input.OffsetMapping.Identity)
                    }
                )
                
                if (content.text.isEmpty()) {
                    Text(
                        text = "Start typing...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp) // Align with text
                    )
                }
            }
        }
    }
}

// Helper to insert formatting characters around selection
fun insertFormatting(value: TextFieldValue, prefix: String, suffix: String): TextFieldValue {
    val text = value.text
    val selection = value.selection

    val before = text.substring(0, selection.start)
    val selected = text.substring(selection.start, selection.end)
    val after = text.substring(selection.end)

    val newText = "$before$prefix$selected$suffix$after"
    
    // Move cursor to end of inserted text (or inside if no selection)
    val newCursorPos = if (selected.isEmpty()) {
        selection.start + prefix.length
    } else {
        selection.end + prefix.length + suffix.length
    }

    return TextFieldValue(
        text = newText,
        selection = androidx.compose.ui.text.TextRange(newCursorPos)
    )
}

// Simple Markdown Parser
fun renderMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        val boldRegex = "\\*([^*]+)\\*".toRegex()
        val italicRegex = "_([^_]+)_".toRegex()
        
        // We need to parse sequentially. For simplicity in this "WhatsApp-like" implementation,
        // we'll just apply styles to the raw text including the markers, 
        // OR we can try to hide markers. Hiding markers is complex with OffsetMapping.
        // Let's just style the text INCLUDING markers for now, like WhatsApp does while typing.
        // Actually, WhatsApp hides them after you send, but shows them while typing.
        // Since this is an editor, showing them is correct.
        
        append(text)
        
        // Apply Bold
        boldRegex.findAll(text).forEach { match ->
            addStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black), // Make bold text distinct
                start = match.range.first,
                end = match.range.last + 1
            )
        }
        
        // Apply Italic
        italicRegex.findAll(text).forEach { match ->
            addStyle(
                style = SpanStyle(fontStyle = FontStyle.Italic),
                start = match.range.first,
                end = match.range.last + 1
            )
        }
    }
}
