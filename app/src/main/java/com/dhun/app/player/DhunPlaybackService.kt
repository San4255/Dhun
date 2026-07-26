package com.dhun.app.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.dhun.app.DhunApp
import com.dhun.app.ui.MainActivity

@OptIn(UnstableApi::class)
class DhunPlaybackService : MediaSessionService() {
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Configure Media3's built-in notification provider so background playback works
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notifProvider = DefaultMediaNotificationProvider.Builder(this)
            .setNotificationId(NOTIFICATION_ID)
            .setChannelId(CHANNEL_ID)
            .build()
        setMediaNotificationProvider(notifProvider)

        val player = DhunApp.instance.player
        session = player.getOrCreateSession(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            val existing = mgr.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val ch = NotificationChannel(
                    CHANNEL_ID,
                    "Now playing",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Media playback controls"
                    setShowBadge(false)
                    enableLights(false)
                    enableVibration(false)
                    setSound(null, null)
                }
                mgr.createNotificationChannel(ch)
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        session?.release()
        session = null
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = session?.player
        if (p != null && p.playWhenReady && p.mediaItemCount > 0) {
            // keep playing
        } else {
            p?.pause()
            stopSelf()
        }
    }

    companion object {
        const val CHANNEL_ID = "dhun_playback"
        const val NOTIFICATION_ID = 1001
    }
}
