package com.shabinder.spotiflyer.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleService
import androidx.media3.common.MediaItem
import androidx.media3.common.util.NotificationUtil
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.exoplayer.source.BaseMediaSource
import androidx.media3.exoplayer.source.MediaSource
import com.shabinder.common.models.TrackDetails
import com.shabinder.spotiflyer.MainActivity
import com.shabinder.spotiflyer.R
import java.io.File

class AudioPlaybackService : LifecycleService() {

    companion object {
        const val EXTRA_TRACK_DETAILS = "track_details"
    }

    private val binder = LocalBinder()
    private lateinit var player: ExoPlayer

    private lateinit var track: TrackDetails

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlaybackService = this@AudioPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        try {
            track = intent?.getParcelableExtra(EXTRA_TRACK_DETAILS)!!
        } catch (e: Exception) {
            return START_NOT_STICKY
        }


        startForegroundService(track)
        // Handle play/pause commands, initialize playback, etc.
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    fun startForegroundService(track: TrackDetails) {
        // Create a notification for the foreground service
        val notification = createNotification(track)
        startForeground(1, notification)

        // Start audio playback using ExoPlayer
        val mediaItem = MediaItem.fromUri(track.outputFilePath)
        player.setMediaItem(mediaItem)
        player.playWhenReady = true
//        player.seekTo(currentItem, playbackPosition)
        player.prepare()
//        println(track.outputMp3Path)
//        println(track.outputFilePath)
//        player.play()
    }

    private fun createNotification(track: TrackDetails): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val play = NotificationCompat.Action(
            null,
            "Play",
            null
        )
        val pause = NotificationCompat.Action(
            null,
            "Play",
            null
        )

        // Customize the notification as needed
        val builder = NotificationCompat.Builder(this, "media_playback")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(track.title)
            .setOngoing(player.isPlaying)
//            .setContentText(track.)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Adjust priority as needed
            .addAction(play)
            .addAction(pause)

        val notification = builder.build()
//        notification.

        // Create the notification channel if not already created
        NotificationUtil.createNotificationChannel(
            this,
            "media_playback",
            R.string.media_playback,
            0,
            NotificationUtil.IMPORTANCE_HIGH
        )

        return notification
    }

    // Implement other methods for playback control, e.g., pause, stop, seek, etc.
}