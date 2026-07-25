package com.dhun.app.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.dhun.app.DhunApp

@OptIn(UnstableApi::class)
class DhunPlaybackService : MediaSessionService() {
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        session = DhunApp.instance.player.ensureSession(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Now playing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls"
                setShowBadge(false)
            }
            mgr.createNotificationChannel(ch)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        session?.release()
        session = null
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "dhun_playback"
    }
}
