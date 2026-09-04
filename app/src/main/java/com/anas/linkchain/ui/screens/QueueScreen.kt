package com.anas.linkchain.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anas.linkchain.domain.model.ItemStatus
import com.anas.linkchain.domain.model.QueueItem
import com.anas.linkchain.ui.theme.*
import com.anas.linkchain.ui.viewmodel.QueueViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QueueScreen(viewModel: QueueViewModel) {
    val context = LocalContext.current
    val queueItems by viewModel.queueItems.collectAsState()
    val fixedQuality by viewModel.fixedQuality.collectAsState()
    val storageLow by viewModel.storageLow.collectAsState()
    val lowStorageWarnSetting by viewModel.lowStorageWarnSetting.collectAsState()

    var inputFields by remember { mutableStateOf(listOf("")) }

    // Multi-select state: set of selected item IDs
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val isSelectionMode = selectedIds.isNotEmpty()

    // Keep selection in sync when items are removed from DB
    LaunchedEffect(queueItems) {
        val validIds = queueItems.map { it.id }.toSet()
        selectedIds = selectedIds.intersect(validIds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ── Multi-select action bar ──────────────────────────────────────
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface2),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkAccent, RoundedCornerShape(12.dp))
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedIds.size} selected",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkAccent,
                        modifier = Modifier.weight(1f)
                    )
                    // Pause selected
                    IconButton(onClick = {
                        viewModel.pauseItems(selectedIds.toList())
                        selectedIds = emptySet()
                    }) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause selected", tint = DarkPaused)
                    }
                    // Resume selected
                    IconButton(onClick = {
                        viewModel.resumeItems(selectedIds.toList())
                        selectedIds = emptySet()
                    }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume selected", tint = DarkOk)
                    }
                    // Delete selected
                    IconButton(onClick = {
                        viewModel.deleteItems(selectedIds.toList())
                        selectedIds = emptySet()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete selected", tint = DarkErr)
                    }
                    // Cancel selection
                    IconButton(onClick = { selectedIds = emptySet() }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel selection", tint = DarkTextDim)
                    }
                }
            }
        }

        // ── Low storage warning ──────────────────────────────────────────
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

        // ── Add links card ───────────────────────────────────────────────
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

        // ── Queue header ─────────────────────────────────────────────────
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

        if (isSelectionMode) {
            Text(
                text = "Long-press to select • tap to toggle",
                fontSize = 10.sp,
                color = DarkTextFaint,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
            )
        } else {
            Text(
                text = "Long-press any item to enter multi-select",
                fontSize = 10.sp,
                color = DarkTextFaint,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
            )
        }

        // ── Queue list ───────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(queueItems, key = { it.id }) { item ->
                val isSelected = item.id in selectedIds

                QueueItemRow(
                    item = item,
                    isSelected = isSelected,
                    isSelectionMode = isSelectionMode,
                    onTogglePause = { viewModel.togglePause(item) },
                    onDelete = { viewModel.deleteItem(item) },
                    onLongPress = {
                        selectedIds = if (isSelected) selectedIds - item.id else selectedIds + item.id
                    },
                    onTap = {
                        if (isSelectionMode) {
                            selectedIds = if (isSelected) selectedIds - item.id else selectedIds + item.id
                        }
                    }
                )
            }
        }

        // ── Start batch button ───────────────────────────────────────────
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QueueItemRow(
    item: QueueItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onTogglePause: () -> Unit,
    onDelete: () -> Unit,
    onLongPress: () -> Unit,
    onTap: () -> Unit
) {
    val statusColor = when (item.status) {
        ItemStatus.PENDING -> DarkPending
        ItemStatus.DOWNLOADING -> DarkAccent
        ItemStatus.PAUSED -> DarkPaused
        ItemStatus.DONE -> DarkOk
        ItemStatus.FAILED -> DarkErr
    }

    val borderColor = if (isSelected) DarkAccent else DarkBorderSoft
    val bgColor = if (isSelected) DarkAccent.copy(alpha = 0.08f) else DarkSurface

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator OR status dot
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onLongPress() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = DarkAccent,
                        uncheckedColor = DarkTextDim,
                        checkmarkColor = DarkBg
                    ),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            // URL + status text
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

            // Pause / Resume button (PENDING & PAUSED only)
            if (item.status == ItemStatus.PENDING || item.status == ItemStatus.PAUSED) {
                IconButton(
                    onClick = onTogglePause,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (item.status == ItemStatus.PAUSED)
                            Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (item.status == ItemStatus.PAUSED) "Resume" else "Pause",
                        tint = DarkTextDim,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Delete button (always visible, disabled while DOWNLOADING)
            IconButton(
                onClick = onDelete,
                enabled = item.status != ItemStatus.DOWNLOADING,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = if (item.status == ItemStatus.DOWNLOADING) DarkTextFaint else DarkErr,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
