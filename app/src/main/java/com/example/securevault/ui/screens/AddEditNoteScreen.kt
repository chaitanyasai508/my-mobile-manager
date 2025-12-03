package com.example.securevault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.securevault.ui.components.richtext.*
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
    var content by remember {
        mutableStateOf(
            TextFieldValue(
                text = existingNote?.content ?: "",
                selection = TextRange((existingNote?.content?.length ?: 0))
            )
        )
    }
    var showToolbar by remember { mutableStateOf(true) }

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
        },
        bottomBar = {
            FormattingToolbar(
                onInsertFormat = { formatType ->
                    content = insertFormatting(content, formatType)
                },
                visible = showToolbar,
                onToggleVisibility = { showToolbar = !showToolbar }
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
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content Input
            OutlinedTextField(
                value = content,
                onValueChange = { newValue ->
                    // Check if Enter was pressed
                    if (newValue.text.length > content.text.length &&
                        newValue.text.getOrNull(newValue.selection.start - 1) == '\n'
                    ) {
                        // Try smart list continuation
                        val smartResult = handleEnterKey(content)
                        if (smartResult != null) {
                            content = smartResult
                            return@OutlinedTextField
                        }
                    }
                    content = newValue
                },
                label = { Text("Content") },
                placeholder = {
                    Text("Type your note here...\n\nUse *bold* _italic_ ~strike~\n- Bullet lists\n1. Numbered lists")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                ),
                maxLines = Int.MAX_VALUE
            )
        }
    }
}
