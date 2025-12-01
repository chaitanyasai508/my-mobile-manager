package com.example.securevault.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    if (bills.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No bills found. Add one!")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(bills) { bill ->
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
