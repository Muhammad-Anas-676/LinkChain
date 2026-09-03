package com.anas.linkchain.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anas.linkchain.domain.model.ItemStatus
import com.anas.linkchain.domain.model.QueueItem
import com.anas.linkchain.ui.theme.*
import com.anas.linkchain.ui.viewmodel.QueueViewModel

@Composable
fun QueueScreen(viewModel: QueueViewModel) {
    val context = LocalContext.current
    val queueItems by viewModel.queueItems.collectAsState()
    val fixedQuality by viewModel.fixedQuality.collectAsState()
    val storageLow by viewModel.storageLow.collectAsState()
    val lowStorageWarnSetting by viewModel.lowStorageWarnSetting.collectAsState()

    var inputFields by remember { mutableStateOf(listOf("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (storageLow && lowStorageWarnSetting) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkErr.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Low storage space available (<500MB). Downloads may fail.",
                        color = DarkErr,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.dismissStorageWarning() }) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = DarkErr)
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "ADD LINKS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkAccent
                )

                Spacer(modifier = Modifier.height(8.dp))

                inputFields.forEachIndexed { index, urlText ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = urlText,
                            onValueChange = { newText ->
                                inputFields = inputFields.toMutableList().also { it[index] = newText }
                            },
                            label = { Text("Link ${index + 1}", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        if (inputFields.size > 1) {
                            IconButton(onClick = {
                                inputFields = inputFields.toMutableList().also { it.removeAt(index) }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = DarkTextDim)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { inputFields = inputFields + "" }) {
                        Text("+ Add another", color = DarkAccent, fontSize = 13.sp)
                    }
                    Button(
                        onClick = {
                            viewModel.addLinksToQueue(inputFields) { added, skipped ->
                                Toast.makeText(context, "$added added · $skipped skipped", Toast.LENGTH_SHORT).show()
                                inputFields = listOf("")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkAccent)
                    ) {
                        Text("Add to queue", color = DarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Queue (${queueItems.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface2,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Text(
                    text = "Quality: $fixedQuality",
                    fontSize = 11.sp,
                    color = DarkTextDim,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(queueItems, key = { it.id }) { item ->
                QueueItemRow(item = item, onTogglePause = { viewModel.togglePause(item) })
            }
        }

        Button(
            onClick = { viewModel.startBatch() },
            enabled = queueItems.any { it.status == ItemStatus.PENDING },
            colors = ButtonDefaults.buttonColors(containerColor = DarkAccent),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Start batch download", color = DarkBg, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun QueueItemRow(item: QueueItem, onTogglePause: () -> Unit) {
    val statusColor = when (item.status) {
        ItemStatus.PENDING -> DarkPending
        ItemStatus.DOWNLOADING -> DarkAccent
        ItemStatus.PAUSED -> DarkPaused
        ItemStatus.DONE -> DarkOk
        ItemStatus.FAILED -> DarkErr
    }

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
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

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
                    color = DarkTextDim
                )
            }

            if (item.status == ItemStatus.PENDING || item.status == ItemStatus.PAUSED) {
                IconButton(onClick = onTogglePause) {
                    Icon(
                        imageVector = if (item.status == ItemStatus.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause/Resume",
                        tint = DarkTextDim
                    )
                }
            }
        }
    }
}