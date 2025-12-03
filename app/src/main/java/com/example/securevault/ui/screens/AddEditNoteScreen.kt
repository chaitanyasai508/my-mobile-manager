package com.example.securevault.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.securevault.ui.components.richtext.*
import com.example.securevault.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

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
    var content by remember { mutableStateOf(TextFieldValue(existingNote?.content ?: "")) }
    var showFormatMenu by remember { mutableStateOf(false) }

    // Parsed content for display
    val parsedContent = remember(content.text) {
        RichTextParser.parse(content.text)
    }

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

            // Hint text
            Text(
                text = "Formatting: *bold* _italic_ ~strike~ `code` - bullet 1. numbered",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Rich Text Content Input
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    // Show formatting menu if there's a selection
                                    if (content.selection.start != content.selection.end) {
                                        showFormatMenu = true
                                    }
                                }
                            )
                        }
                ) {
                    BasicTextField(
                        value = content,
                        onValueChange = { newValue ->
                            // Handle special keys
                            if (newValue.text.length > content.text.length) {
                                val addedChar = newValue.text[newValue.selection.start - 1]
                                if (addedChar == '\n') {
                                    // Handle Enter key for smart list continuation
                                    val (newText, newCursor) = RichTextUtils.handleEnterKey(
                                        content.text,
                                        content.selection.start - 1
                                    )
                                    content = TextFieldValue(
                                        text = newText,
                                        selection = TextRange(newCursor)
                                    )
                                    return@BasicTextField
                                }
                            }
                            content = newValue
                        },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Default
                        ),
                        decorationBox = { innerTextField ->
                            if (content.text.isEmpty()) {
                                Text(
                                    text = "Start typing...\n\nTip: Long-press selected text for formatting menu",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = TextStyle(fontSize = 16.sp, lineHeight = 24.sp)
                                )
                            }
                            // Show the parsed/formatted text as overlay
                            if (content.text.isNotEmpty()) {
                                Text(
                                    text = parsedContent,
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                            // The actual input field (invisible but receives input)
                            Box(modifier = Modifier.fillMaxSize()) {
                                innerTextField()
                            }
                        }
                    )
                }
            }

            // Formatting Context Menu
            FormattingContextMenu(
                visible = showFormatMenu,
                onDismiss = { showFormatMenu = false },
                onFormatSelected = { formatType ->
                    val (newText, newCursor) = applyFormatting(
                        content.text,
                        content.selection.start,
                        content.selection.end,
                        formatType
                    )
                    content = TextFieldValue(
                        text = newText,
                        selection = TextRange(newCursor)
                    )
                }
            )
        }
    }
}
