package com.anas.linkchain

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.anas.linkchain.data.local.datastore.PreferencesManager
import com.anas.linkchain.data.local.db.AppDatabase

class LinkChainApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
        preferencesManager = PreferencesManager(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_QUEUE_ID,
                "LinkChain Queue Runner",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows sequential queue progress"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_QUEUE_ID = "linkchain_queue_channel"
        lateinit var instance: LinkChainApp
            private set
    }
}