package com.example.securevault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.securevault.ui.theme.Spacing
import com.example.securevault.ui.viewmodel.MainViewModel
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults

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
    val richTextState = rememberRichTextState()
    
    // Load existing content
    LaunchedEffect(existingNote) {
        existingNote?.content?.let { content ->
            richTextState.setHtml(content)
        }
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
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                val htmlContent = richTextState.toHtml()
                                if (noteId == null) {
                                    viewModel.addNote(title, htmlContent)
                                } else {
                                    viewModel.updateNote(noteId, title, htmlContent)
                                }
                                onNavigateBack()
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            RichTextToolbar(richTextState = richTextState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("Note title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            // Rich Text Editor
            RichTextEditor(
                state = richTextState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge,
                placeholder = {
                    Text(
                        text = "Start typing your note...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                colors = RichTextEditorDefaults.richTextEditorColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun RichTextToolbar(
    richTextState: com.mohamedrejeb.richeditor.model.RichTextState
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.small, vertical = Spacing.small),
            horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            // Bold
            item {
                ToolbarButton(
                    icon = Icons.Default.FormatBold,
                    contentDescription = "Bold",
                    isActive = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold,
                    onClick = {
                        richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    }
                )
            }
            
            // Italic
            item {
                ToolbarButton(
                    icon = Icons.Default.FormatItalic,
                    contentDescription = "Italic",
                    isActive = richTextState.currentSpanStyle.fontStyle == FontStyle.Italic,
                    onClick = {
                        richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    }
                )
            }
            
            // Divider
            item {
                VerticalDivider(
                    modifier = Modifier
                        .height(40.dp)
                        .padding(horizontal = 4.dp)
                )
            }
            
            // H1
            item {
                ToolbarButton(
                    text = "H1",
                    contentDescription = "Heading 1",
                    isActive = richTextState.currentSpanStyle.fontSize == 24.sp,
                    onClick = {
                        richTextState.toggleSpanStyle(
                            SpanStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                )
            }
            
            // H2
            item {
                ToolbarButton(
                    text = "H2",
                    contentDescription = "Heading 2",
                    isActive = richTextState.currentSpanStyle.fontSize == 20.sp,
                    onClick = {
                        richTextState.toggleSpanStyle(
                            SpanStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                )
            }
            
            // H3
            item {
                ToolbarButton(
                    text = "H3",
                    contentDescription = "Heading 3",
                    isActive = richTextState.currentSpanStyle.fontSize == 18.sp,
                    onClick = {
                        richTextState.toggleSpanStyle(
                            SpanStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                )
            }
            
            // Divider
            item {
                VerticalDivider(
                    modifier = Modifier
                        .height(40.dp)
                        .padding(horizontal = 4.dp)
                )
            }
            
            // Unordered List
            item {
                ToolbarButton(
                    icon = Icons.Default.FormatListBulleted,
                    contentDescription = "Bullet List",
                    isActive = richTextState.isUnorderedList,
                    onClick = {
                        richTextState.toggleUnorderedList()
                    }
                )
            }
            
            // Ordered List
            item {
                ToolbarButton(
                    icon = Icons.Default.FormatListNumbered,
                    contentDescription = "Numbered List",
                    isActive = richTextState.isOrderedList,
                    onClick = {
                        richTextState.toggleOrderedList()
                    }
                )
            }
            
            // Divider
            item {
                VerticalDivider(
                    modifier = Modifier
                        .height(40.dp)
                        .padding(horizontal = 4.dp)
                )
            }
            
            // Link
            item {
                ToolbarButton(
                    icon = Icons.Default.Link,
                    contentDescription = "Add Link",
                    isActive = richTextState.isLink,
                    onClick = {
                        // For simplicity, using a basic link
                        // In a real app, you'd show a dialog to get URL
                        if (!richTextState.isLink) {
                            richTextState.addLink(
                                text = "Link",
                                url = "https://example.com"
                            )
                        }
                    }
                )
            }
            
            // Code Block
            item {
                ToolbarButton(
                    icon = Icons.Default.Code,
                    contentDescription = "Code Block",
                    isActive = richTextState.isCodeSpan,
                    onClick = {
                        richTextState.toggleCodeSpan()
                    }
                )
            }
        }
    }
}

@Composable
fun ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    text: String? = null,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isActive)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.size(40.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp)
            )
        } else if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
