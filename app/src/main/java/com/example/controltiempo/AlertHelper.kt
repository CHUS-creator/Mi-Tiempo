package com.example.controltiempo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import androidx.core.app.NotificationCompat

object AlertHelper {

    private const val CHANNEL_ID = "block_alerts"
    private const val NOTIFICATION_ID = 1001

    private var soundPool: SoundPool? = null
    private var blockStartSound = 0
    private var blockEndSound = 0

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.alerts_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.alerts_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    fun ensureSoundsLoaded(context: Context) {
        if (soundPool != null) return
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attributes)
            .build()
        blockStartSound = soundPool!!.load(context, R.raw.block_start, 1)
        blockEndSound = soundPool!!.load(context, R.raw.block_end, 1)
    }

    fun playBlockStart(context: Context) {
        ensureSoundsLoaded(context)
        soundPool?.play(blockStartSound, 1f, 1f, 1, 0, 1f)
    }

    fun playBlockEnd(context: Context) {
        ensureSoundsLoaded(context)
        soundPool?.play(blockEndSound, 1f, 1f, 1, 0, 1f)
    }

    fun notifyBlock(context: Context, title: String, text: String) {
        ensureChannel(context)
        val manager = context.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
        }
    }

    fun cancelNotifications(context: Context) {
        context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }
}
