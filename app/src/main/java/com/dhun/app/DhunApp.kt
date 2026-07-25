package com.dhun.app

import android.app.Application
import com.dhun.app.player.DhunPlayer
import com.dhun.app.data.LibraryRepository
import com.dhun.app.data.Prefs

class DhunApp : Application() {

    lateinit var library: LibraryRepository
        private set
    lateinit var player: DhunPlayer
        private set
    lateinit var prefs: Prefs
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        prefs = Prefs(this)
        library = LibraryRepository(this)
        player = DhunPlayer(this)
    }

    companion object {
        lateinit var instance: DhunApp
            private set
    }
}
