package com.shabinder.spotiflyer.service

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class MusicPlayerService : MediaLibraryService() {// MediaSessionService() {

    private lateinit var player: Player
    private lateinit var session: MediaLibrarySession

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(applicationContext)
            .setRenderersFactory(
                DefaultRenderersFactory(this).setExtensionRendererMode(
                    DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER /* We prefer extensions, such as FFmpeg */
                )
            ).build()

        session = MediaLibrarySession.Builder(this, player,
            object : MediaLibrarySession.Callback {
                override fun onAddMediaItems(
                    mediaSession: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    mediaItems: MutableList<MediaItem>
                ): ListenableFuture<MutableList<MediaItem>> {

                    /* This is the trickiest part, if you don't do this here, nothing will play */
                    val updatedMediaItems =
                        mediaItems.map { it.buildUpon().setUri(it.mediaId).build() }.toMutableList()
                    return Futures.immediateFuture(updatedMediaItems)
                }
            }).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return session
    }
}