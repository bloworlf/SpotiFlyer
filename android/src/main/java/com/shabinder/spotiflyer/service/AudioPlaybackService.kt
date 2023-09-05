package com.shabinder.spotiflyer.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.media3.common.MediaItem
import androidx.media3.common.util.NotificationUtil
import androidx.media3.exoplayer.ExoPlayer
import com.shabinder.common.models.TrackDetails
import com.shabinder.spotiflyer.MainActivity
import com.shabinder.spotiflyer.R
import com.shabinder.spotiflyer.broadcast.AudioPlaybackReceiver

class AudioPlaybackService : LifecycleService() {

    companion object {
        const val EXTRA_TRACK_DETAILS = "track_details"
        const val EXTRA_TRACK_POSITION = "track_position"

        //        const val EXTRA_TRACK_ACTION = "track_action"
        const val EXTRA_PAUSE_TRACK = "pause_track"
    }

    private val binder = LocalBinder()
    private lateinit var player: ExoPlayer

    private lateinit var track: TrackDetails
//    private var playing: Boolean = false

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

        val pause_track = intent.getBooleanExtra(EXTRA_PAUSE_TRACK, false)
        val currentPosition = intent.getLongExtra(EXTRA_TRACK_POSITION, 0L)

        if (pause_track) {
            player.pause()
        } else {
            // Start audio playback using ExoPlayer
            val mediaItem = MediaItem.fromUri(track.outputFilePath)
            player.setMediaItem(mediaItem)
            player.playWhenReady = true
//        player.seekTo(currentItem, playbackPosition)
            player.prepare()
//        println(track.outputMp3Path)
//        println(track.outputFilePath)
//        player.play()
            player.seekTo(currentPosition)
        }

        startForegroundService(track, pause_track)
        // Handle play/pause commands, initialize playback, etc.
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    fun startForegroundService(track: TrackDetails, pause: Boolean) {
        // Create a notification for the foreground service
        val notification = createNotification(track, pause)
        startForeground(1, notification)
    }

    private fun createNotification(track: TrackDetails, pause: Boolean): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

//        val playIntent = Intent(AudioPlaybackReceiver.PLAY_ACTION).apply {
//            putExtra(EXTRA_TRACK_DETAILS, track)
//        }
//        val flag =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//                PendingIntent.FLAG_IMMUTABLE
//            else
//                0
//        val playPendingIntent = PendingIntent.getBroadcast(
//            this,
//            0,
//            playIntent,
//            flag
//        )
//        val play = NotificationCompat.Action(
//            null,
//            "Play",
//            playPendingIntent
//        )
//        val pauseIntent = Intent(AudioPlaybackReceiver.PAUSE_ACTION).apply {
//            putExtra(EXTRA_TRACK_DETAILS, track)
//        }
//        val pausePendingIntent = PendingIntent.getBroadcast(
//            this,
//            0,
//            pauseIntent,
//            flag
//        )
//        val pause = NotificationCompat.Action(
//            null,
//            "Pause",
//            pausePendingIntent
//        )

        // Customize the notification as needed
        val builder = NotificationCompat.Builder(this, "media_playback")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(track.title)
            .setOngoing(!pause)
//            .setContentText(track.)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Adjust priority as needed
//            .addAction(play)
//            .addAction(pause)


        if (pause) {
            //add play action
            val ii = Intent(this, AudioPlaybackReceiver::class.java)
            ii.putExtra(EXTRA_TRACK_DETAILS, track)
            ii.putExtra(EXTRA_TRACK_POSITION, player.currentPosition)
            ii.action = AudioPlaybackReceiver.PLAY_ACTION
            val ppIntent = PendingIntent.getBroadcast(
                this,
                0,
                ii,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val action: NotificationCompat.Action = NotificationCompat.Action.Builder(
                null,
                "Play",
                ppIntent
            ).build()
            builder.addAction(action)
        } else {
            //add pause action
            val ii = Intent(this, AudioPlaybackReceiver::class.java)
            ii.putExtra(EXTRA_TRACK_DETAILS, track)
            ii.action = AudioPlaybackReceiver.PAUSE_ACTION
            val ppIntent = PendingIntent.getBroadcast(
                this,
                0,
                ii,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val action: NotificationCompat.Action = NotificationCompat.Action.Builder(
                null,
                "Pause",
                ppIntent
            ).build()
            builder.addAction(action)
        }


        val notification = builder.build()
//        notification.

        // Create the notification channel if not already created
        NotificationUtil.createNotificationChannel(
            this,
            "media_playback",
            R.string.media_playback,
            0,
            NotificationUtil.IMPORTANCE_DEFAULT //if (pause) NotificationUtil.IMPORTANCE_LOW else NotificationUtil.IMPORTANCE_HIGH
        )

        return notification
    }

    // Implement other methods for playback control, e.g., pause, stop, seek, etc.
}