package com.dhun.app

import android.app.Application
import android.content.ComponentName
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.dhun.app.data.LibraryRepository
import com.dhun.app.data.Prefs
import com.dhun.app.player.DhunPlaybackService
import com.dhun.app.player.DhunPlayer
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import java.util.concurrent.Executors

class DhunApp : Application() {

    lateinit var library: LibraryRepository
        private set
    lateinit var player: DhunPlayer
        private set
    lateinit var prefs: Prefs
        private set

    private var controllerFuture: ListenableFuture<MediaController>? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        prefs = Prefs(this)
        library = LibraryRepository(this)
        player = DhunPlayer(this)
        // Bind a persistent MediaController to our service so Media3 promotes the
        // service to foreground and displays a media-style notification when playing
        // (with title/artist, album art, play/pause/next/prev, lockscreen controls).
        val token = SessionToken(this, ComponentName(this, DhunPlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        future.addListener({
            // keep a strong ref; controller connection is what triggers the notification
            runCatching { future.get() }
        }, MoreExecutors.directExecutor())
        controllerFuture = future
    }

    override fun onTerminate() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        player.release()
        super.onTerminate()
    }

    companion object {
        lateinit var instance: DhunApp
            private set
    }
}
