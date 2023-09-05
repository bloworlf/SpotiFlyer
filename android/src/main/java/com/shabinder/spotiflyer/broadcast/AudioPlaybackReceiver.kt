package com.shabinder.spotiflyer.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.shabinder.common.models.TrackDetails
import com.shabinder.spotiflyer.service.AudioPlaybackService

class AudioPlaybackReceiver : BroadcastReceiver() {

    private var currentPosition: Long = 0L

    companion object {
        const val PAUSE_ACTION = "com.shabinder.spotiflyer.AudioPlayback.PAUSE"
        const val PLAY_ACTION = "com.shabinder.spotiflyer.AudioPlayback.PLAY"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val track =
                it.getParcelableExtra<TrackDetails>(AudioPlaybackService.EXTRA_TRACK_DETAILS)
                    ?: return

            val serviceIntent =
                Intent(context, AudioPlaybackService::class.java).apply {
                    putExtra(AudioPlaybackService.EXTRA_TRACK_DETAILS, track)
                }
            when (it.action) {
                PAUSE_ACTION -> {
                    currentPosition =
                        intent.getLongExtra(AudioPlaybackService.EXTRA_TRACK_POSITION, 0L)
                    serviceIntent.putExtra(AudioPlaybackService.EXTRA_PAUSE_TRACK, true)
                }

                PLAY_ACTION -> {
                    serviceIntent.putExtra(AudioPlaybackService.EXTRA_TRACK_POSITION, currentPosition)
                    serviceIntent.putExtra(AudioPlaybackService.EXTRA_PAUSE_TRACK, false)
                }

                else -> {}
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(serviceIntent)
            } else {
                context?.startService(serviceIntent)
            }
        }
    }
}