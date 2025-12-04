package com.example.securevault.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.securevault.data.BillRepository
import com.example.securevault.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BillsListScreen(
    viewModel: MainViewModel,
    onAddClick: () -> Unit,
    onItemClick: (Int) -> Unit
) {
    val bills by viewModel.bills.collectAsState()
    
    // Month selector state
    val currentCalendar = Calendar.getInstance()
    var selectedMonth by remember { mutableStateOf(currentCalendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(currentCalendar.get(Calendar.YEAR)) }
    var showMonthPicker by remember { mutableStateOf(false) }
    
    // Filter bills for selected month
    val billsForMonth = remember(bills, selectedMonth, selectedYear) {
        getBillsForMonth(bills, selectedMonth, selectedYear)
    }
    
    // Calculate total
    val totalAmount = remember(billsForMonth) {
        billsForMonth.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    }
    
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val selectedDate = Calendar.getInstance().apply {
        set(Calendar.YEAR, selectedYear)
        set(Calendar.MONTH, selectedMonth)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Month Selector Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Bills for",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = monthFormat.format(selectedDate.time),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Row {
                        IconButton(onClick = {
                            if (selectedMonth == 0) {
                                selectedMonth = 11
                                selectedYear--
                            } else {
                                selectedMonth--
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Previous Month",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = {
                            if (selectedMonth == 11) {
                                selectedMonth = 0
                                selectedYear++
                            } else {
                                selectedMonth++
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Next Month",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Total amount
                Text(
                    text = "Total: $${"%.2f".format(totalAmount)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "${billsForMonth.size} bill(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (billsForMonth.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No bills due this month")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(billsForMonth) { bill ->
                    BillCard(
                        billName = bill.billName,
                        amount = bill.amount,
                        dueDate = bill.dueDate,
                        frequency = bill.frequency,
                        onClick = { onItemClick(bill.id) }
                    )
                }
            }
        }
    }

    // FAB
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Bill")
        }
    }
}

// Helper function to get bills for a specific month
private fun getBillsForMonth(
    bills: List<BillRepository.DomainBill>,
    month: Int,
    year: Int
): List<BillRepository.DomainBill> {
    val targetCalendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    
    val monthStart = targetCalendar.timeInMillis
    val monthEnd = targetCalendar.apply {
        add(Calendar.MONTH, 1)
        add(Calendar.MILLISECOND, -1)
    }.timeInMillis
    
    return bills.filter { bill ->
        // Check if the bill's due date falls in this month
        val billCalendar = Calendar.getInstance().apply {
            timeInMillis = bill.dueDate
        }
        
        val billMonth = billCalendar.get(Calendar.MONTH)
        val billYear = billCalendar.get(Calendar.YEAR)
        
        // Direct match
        if (billMonth == month && billYear == year) {
            return@filter true
        }
        
        // For recurring bills, check if they recur in this month
        when (bill.frequency) {
            "MONTHLY" -> {
                // Monthly bills recur every month
                billYear <= year && (billYear < year || billMonth <= month)
            }
            "QUARTERLY" -> {
                // Check if this month is a quarter month for this bill
                val monthsSinceStart = (year - billYear) * 12 + (month - billMonth)
                monthsSinceStart >= 0 && monthsSinceStart % 3 == 0
            }
            "SEMI_ANNUAL" -> {
                // Check if this month is a semi-annual month
                val monthsSinceStart = (year - billYear) * 12 + (month - billMonth)
                monthsSinceStart >= 0 && monthsSinceStart % 6 == 0
            }
            "ANNUAL" -> {
                // Check if this month matches the bill month
                billMonth == month && billYear <= year
            }
            else -> false
        }
    }
}

@Composable
fun BillCard(
    billName: String,
    amount: String,
    dueDate: Long,
    frequency: String,
    onClick: () -> Unit
) {
    val frequencyColor = when (frequency) {
        "MONTHLY" -> Color(0xFF2196F3) // Blue
        "QUARTERLY" -> Color(0xFF4CAF50) // Green
        "SEMI_ANNUAL" -> Color(0xFFFF9800) // Orange
        "ANNUAL" -> Color(0xFF9C27B0) // Purple
        else -> Color.Gray
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dueDateStr = dateFormat.format(Date(dueDate))
    
    // Calculate days until due
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    val daysUntil = ((dueDate - today) / (1000 * 60 * 60 * 24)).toInt()
    val daysText = when {
        daysUntil < 0 -> "Overdue"
        daysUntil == 0 -> "Due today"
        daysUntil == 1 -> "Due tomorrow"
        else -> "Due in $daysUntil days"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = billName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$$amount",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dueDateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = daysText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (daysUntil < 0) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Frequency badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = frequencyColor.copy(alpha = 0.2f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = frequency.replace("_", " "),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = frequencyColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


