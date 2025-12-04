package com.example.securevault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.securevault.ui.theme.Spacing
import com.example.securevault.ui.viewmodel.MainViewModel

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    val isSetupRequired by viewModel.isSetupRequired.collectAsState()
    val isAuthenticated by viewModel.isUserAuthenticated.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onLoginSuccess()
        }
    }

    // Auto-submit when PIN is complete
    LaunchedEffect(pin) {
        if (pin.length == 6 && !isSetupRequired) {
            viewModel.verifyMasterPassword(pin)
        }
    }

    LaunchedEffect(confirmPin) {
        if (confirmPin.length == 6 && isSetupRequired && pin == confirmPin) {
            viewModel.setupMasterPassword(pin)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(Spacing.large))
            
            Text(
                text = if (isSetupRequired) "Create 6-Digit PIN" else "Enter PIN",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(Spacing.small))
            
            Text(
                text = if (isSetupRequired) "Set a 6-digit PIN to protect your data" else "Enter your 6-digit PIN",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.huge))

        // PIN Input
        PinInput(
            pin = pin,
            onPinChange = { newPin ->
                if (newPin.length <= 6 && newPin.all { it.isDigit() }) {
                    pin = newPin
                }
            },
            label = if (isSetupRequired) "Enter PIN" else "PIN"
        )

            if (isSetupRequired) {
                Spacer(modifier = Modifier.height(Spacing.extraLarge))
                PinInput(
                    pin = confirmPin,
                    onPinChange = { newPin ->
                        if (newPin.length <= 6 && newPin.all { it.isDigit() }) {
                            confirmPin = newPin
                        }
                    },
                    label = "Confirm PIN",
                    isError = confirmPin.isNotEmpty() && confirmPin != pin
                )
            }
            
            if (uiState is MainViewModel.UiState.Error) {
                Spacer(modifier = Modifier.height(Spacing.medium))
                Text(
                    text = (uiState as MainViewModel.UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(Spacing.extraLarge))

            // Clear button
            OutlinedButton(
                onClick = {
                    pin = ""
                    confirmPin = ""
                },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Clear")
            }
        }
    }
}

@Composable
fun PinInput(
    pin: String,
    onPinChange: (String) -> Unit,
    label: String,
    isError: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }

    // Request focus when the screen is shown
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(contentAlignment = Alignment.Center) {
            // Hidden text field for keyboard input
            // Must have size > 0 to receive focus events properly on some devices
            BasicTextField(
                value = pin,
                onValueChange = onPinChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0f)
                    .focusRequester(focusRequester)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    PinDigitBox(
                        digit = pin.getOrNull(index)?.toString() ?: "",
                        isFocused = pin.length == index,
                        isError = isError,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { focusRequester.requestFocus() }
                    )
                }
            }
        }
    }
}

@Composable
fun PinDigitBox(
    digit: String,
    isFocused: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        digit.isNotEmpty() -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    
    val backgroundColor = if (digit.isNotEmpty()) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = backgroundColor,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (digit.isNotEmpty()) {
                    Text(
                        text = "●",
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
