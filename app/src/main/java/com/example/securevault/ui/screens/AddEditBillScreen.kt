package com.example.securevault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.securevault.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBillScreen(
    viewModel: MainViewModel,
    billId: Int,
    onBack: () -> Unit
) {
    var billName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var frequency by remember { mutableStateOf("MONTHLY") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showFrequencyMenu by remember { mutableStateOf(false) }

    val isEditMode = billId != -1

    LaunchedEffect(billId) {
        if (isEditMode) {
            val bill = viewModel.bills.value.find { it.id == billId }
            if (bill != null) {
                billName = bill.billName
                amount = bill.amount
                notes = bill.notes
                dueDate = bill.dueDate
                frequency = bill.frequency
            }
        }
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Bill" else "Add Bill") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = {
                            viewModel.deleteBill(billId)
                            onBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = billName,
                onValueChange = { billName = it },
                label = { Text("Bill Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("$") }
            )

            // Date Picker Button
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Due Date: ${dateFormat.format(Date(dueDate))}")
            }

            // Frequency Selector
            ExposedDropdownMenuBox(
                expanded = showFrequencyMenu,
                onExpandedChange = { showFrequencyMenu = it }
            ) {
                OutlinedTextField(
                    value = frequency.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frequency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFrequencyMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showFrequencyMenu,
                    onDismissRequest = { showFrequencyMenu = false }
                ) {
                    listOf("MONTHLY", "QUARTERLY", "SEMI_ANNUAL", "ANNUAL").forEach { freq ->
                        DropdownMenuItem(
                            text = { Text(freq.replace("_", " ")) },
                            onClick = {
                                frequency = freq
                                showFrequencyMenu = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    if (billName.isNotBlank() && amount.isNotBlank()) {
                        if (isEditMode) {
                            viewModel.updateBill(billId, billName, amount, notes, dueDate, frequency)
                        } else {
                            viewModel.addBill(billName, amount, notes, dueDate, frequency)
                        }
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueDate = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
