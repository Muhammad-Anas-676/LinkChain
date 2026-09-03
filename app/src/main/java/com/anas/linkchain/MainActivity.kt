package com.anas.linkchain

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.anas.linkchain.ui.screens.MainScreen
import com.anas.linkchain.ui.theme.DarkBg
import com.anas.linkchain.ui.theme.LinkChainTheme
import com.anas.linkchain.ui.viewmodel.QueueViewModel
import com.anas.linkchain.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val queueViewModel: QueueViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleShareIntent(intent)

        setContent {
            val isDark by settingsViewModel.isDarkTheme.collectAsState()
            LinkChainTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize(), color = DarkBg) {
                    MainScreen(
                        queueViewModel = queueViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
            lifecycleScope.launch {
                val accept = LinkChainApp.instance.preferencesManager.acceptSharedLinks.first()
                if (accept) {
                    queueViewModel.addLinksToQueue(listOf(sharedText)) { added, _ ->
                        Toast.makeText(this@MainActivity, "Link added to queue", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Link sharing is disabled in settings", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}