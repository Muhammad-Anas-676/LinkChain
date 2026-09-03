package com.anas.linkchain.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anas.linkchain.domain.model.HistoryItem
import com.anas.linkchain.domain.model.ItemStatus
import com.anas.linkchain.ui.theme.*
import com.anas.linkchain.ui.viewmodel.HistoryViewModel
import kotlinx.coroutines.delay

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val historyItems by viewModel.historyItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()

    var isArmed by remember { mutableStateOf(false) }

    LaunchedEffect(isArmed) {
        if (isArmed) {
            delay(3000)
            isArmed = false
        }
    }

    val filtered = historyItems.filter {
        (filterStatus == null || it.status == filterStatus) &&
                (searchQuery.isBlank() || it.url.contains(searchQuery, ignoreCase = true))
    }

    val completedCount = historyItems.count { it.status == ItemStatus.DONE }
    val failedCount = historyItems.count { it.status == ItemStatus.FAILED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Download History", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Button(
                onClick = {
                    if (isArmed) {
                        viewModel.clearAllHistory()
                        isArmed = false
                    } else {
                        isArmed = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isArmed) DarkErr else DarkSurface2)
            ) {
                Text(if (isArmed) "Tap to confirm" else "Clear", fontSize = 12.sp)
            }
        }

        Text(
            text = "$completedCount completed · $failedCount failed",
            fontSize = 12.sp,
            color = DarkTextDim,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            label = { Text("Search by URL...", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterStatus == null,
                onClick = { viewModel.setFilterStatus(null) },
                label = { Text("All", fontSize = 11.sp) }
            )
            FilterChip(
                selected = filterStatus == ItemStatus.DONE,
                onClick = { viewModel.setFilterStatus(ItemStatus.DONE) },
                label = { Text("Completed", fontSize = 11.sp) }
            )
            FilterChip(
                selected = filterStatus == ItemStatus.FAILED,
                onClick = { viewModel.setFilterStatus(ItemStatus.FAILED) },
                label = { Text("Failed", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { item ->
                HistoryRow(item = item, onDelete = { viewModel.deleteItem(item) })
            }
        }

        Text(
            text = "Failed items are retried automatically once the current queue finishes.",
            fontSize = 11.sp,
            color = DarkTextFaint,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun HistoryRow(item: HistoryItem, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorderSoft, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.url,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    maxLines = 1,
                    color = DarkText
                )
                Text(
                    text = "${item.status.name} · ${item.quality} · ${item.platform.name}",
                    fontSize = 10.sp,
                    color = if (item.status == ItemStatus.DONE) DarkOk else DarkErr
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = DarkTextDim)
            }
        }
    }
}