package com.dhun.app.player

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.dhun.app.DhunApp

class DhunPlaybackService : MediaSessionService() {
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        DhunApp.instance.player.ensureSession(this)
        session = DhunApp.instance.player.sessionToken?.let {
            MediaSession.getSession(this, it)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        session = null
        DhunApp.instance.player.release()
        super.onDestroy()
    }
}
