package com.anas.linkchain.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anas.linkchain.service.LinkChainAccessibilityService
import com.anas.linkchain.ui.components.MadeByAnasBadge
import com.anas.linkchain.ui.theme.*
import com.anas.linkchain.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkTheme.collectAsState()
    val quality by viewModel.fixedQuality.collectAsState()
    val wifiOnly by viewModel.wifiOnly.collectAsState()
    val skipDuplicates by viewModel.skipDuplicates.collectAsState()
    val acceptShare by viewModel.acceptSharedLinks.collectAsState()
    val appLock by viewModel.appLockEnabled.collectAsState()
    val lowStorage by viewModel.lowStorageWarn.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("APPEARANCE")
        SettingToggleRow("Dark Theme", isDark) { viewModel.setDarkTheme(it) }

        SectionHeader("NETWORK & STORAGE")
        SettingToggleRow("Wi-Fi Only Downloads", wifiOnly) { viewModel.setWifiOnly(it) }
        SettingToggleRow("Warn on Low Storage (<500MB)", lowStorage) { viewModel.setLowStorageWarn(it) }
        SettingToggleRow("Skip Duplicate Links", skipDuplicates) { viewModel.setSkipDuplicates(it) }

        SectionHeader("DOWNLOAD")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Fixed Quality", fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("1080p", "720p", "480p").forEach { q ->
                    FilterChip(
                        selected = quality == q,
                        onClick = { viewModel.setFixedQuality(q) },
                        label = { Text(q, fontSize = 11.sp) }
                    )
                }
            }
        }

        SectionHeader("AUTOMATION & ACCESSIBILITY")
        SettingToggleRow("Accept Shared Links", acceptShare) { viewModel.setAcceptSharedLinks(it) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Accessibility Service", fontSize = 14.sp)
            Button(
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (LinkChainAccessibilityService.isRunning) DarkOk else DarkErr
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (LinkChainAccessibilityService.isRunning) "Active" else "Enable", fontSize = 12.sp)
            }
        }

        SectionHeader("SECURITY")
        SettingToggleRow("App Lock (PIN)", appLock) { enabled ->
            if (enabled) {
                showPinDialog = true
            } else {
                viewModel.disableAppLock()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            MadeByAnasBadge()
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set 4-Digit PIN") },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            viewModel.setPin(pinInput)
                            showPinDialog = false
                            pinInput = ""
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = DarkAccent,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun SettingToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}