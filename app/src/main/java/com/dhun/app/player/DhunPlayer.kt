package com.dhun.app.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.dhun.app.DhunApp
import com.dhun.app.data.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayState(
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val queue: List<Long> = emptyList(), // song ids
)

class DhunPlayer(private val app: android.app.Application) {

    private var mediaSession: MediaSession? = null
    val exo: ExoPlayer

    private val _state = MutableStateFlow(PlayState())
    val state: StateFlow<PlayState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var posJob: Job? = null

    init {
        exo = ExoPlayer.Builder(app)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        exo.repeatMode = Player.REPEAT_MODE_OFF
        exo.shuffleModeEnabled = false
        exo.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(p: Boolean) { push() }
            override fun onMediaItemTransition(item: MediaItem?, reason: Int) { push() }
            override fun onRepeatModeChanged(rm: Int) { push() }
            override fun onShuffleModeEnabledChanged(shuffle: Boolean) { push() }
        })
        startPositionTicker()
    }

    fun ensureSession(service: MediaSessionService) {
        if (mediaSession == null) {
            val intent = app.packageManager.getLaunchIntentForPackage(app.packageName)?.let {
                PendingIntent.getActivity(app, 0, it, PendingIntent.FLAG_IMMUTABLE)
            }
            mediaSession = MediaSession.Builder(app, exo)
                .setSessionActivity(intent!!)
                .build()
        }
    }

    val sessionToken: MediaSession.Token? get() = mediaSession?.token

    private fun startPositionTicker() {
        posJob?.cancel()
        posJob = scope.launch {
            while (true) {
                push()
                delay(500)
            }
        }
    }

    private fun push() {
        _state.value = PlayState(
            currentIndex = exo.currentMediaItemIndex,
            isPlaying = exo.isPlaying,
            positionMs = exo.currentPosition.coerceAtLeast(0),
            durationMs = exo.duration.takeIf { it > 0 } ?: currentSong()?.durationMs ?: 0L,
            shuffle = exo.shuffleModeEnabled,
            repeatMode = exo.repeatMode,
            queue = queue(),
        )
    }

    fun currentSongId(): Long? {
        val i = exo.currentMediaItemIndex
        if (i == -1 || i >= exo.mediaItemCount) return null
        return exo.getMediaItemAt(i).mediaId.toLongOrNull()
    }

    fun currentSong(): Song? {
        val id = currentSongId() ?: return null
        return DhunApp.instance.library.findSong(id)
    }

    fun play(songs: List<Song>, index: Int = 0) {
        exo.stop()
        exo.clearMediaItems()
        songs.forEach { s ->
            val md = MediaMetadata.Builder()
                .setTitle(s.title)
                .setArtist(s.artist)
                .setAlbumTitle(s.album)
                .build()
            val mi = MediaItem.Builder()
                .setUri(s.uri)
                .setMediaId(s.id.toString())
                .setMediaMetadata(md)
                .build()
            exo.addMediaItem(mi)
        }
        exo.prepare()
        exo.seekTo(index.coerceIn(0, (songs.size - 1).coerceAtLeast(0)), 0)
        exo.play()
        push()
    }

    fun playQueue(queueIds: List<Long>, index: Int = 0) {
        val songs = queueIds.mapNotNull { DhunApp.instance.library.findSong(it) }
        if (songs.isNotEmpty()) play(songs, index)
    }

    fun togglePlayPause() { if (exo.isPlaying) exo.pause() else { if (exo.mediaItemCount == 0) return; exo.play() }; push() }
    fun next() { exo.seekToNextMediaItem(); push() }
    fun prev() {
        if (exo.currentPosition > 5000) exo.seekTo(0) else exo.seekToPreviousMediaItem()
        push()
    }
    fun seekTo(ms: Long) { exo.seekTo(ms); push() }
    fun toggleShuffle() { exo.shuffleModeEnabled = !exo.shuffleModeEnabled; push() }
    fun cycleRepeat() {
        exo.repeatMode = when (exo.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        push()
    }

    fun setSpeed(speed: Float) { exo.setPlaybackSpeed(speed) }

    fun enqueue(songId: Long) {
        val s = DhunApp.instance.library.findSong(songId) ?: return
        val md = MediaMetadata.Builder().setTitle(s.title).setArtist(s.artist).setAlbumTitle(s.album).build()
        val mi = MediaItem.Builder().setUri(s.uri).setMediaId(s.id.toString()).setMediaMetadata(md).build()
        exo.addMediaItem(mi)
        if (exo.mediaItemCount == 1) { exo.prepare(); exo.play() }
        push()
    }

    fun enqueueNext(songId: Long) {
        val s = DhunApp.instance.library.findSong(songId) ?: return
        val md = MediaMetadata.Builder().setTitle(s.title).setArtist(s.artist).setAlbumTitle(s.album).build()
        val mi = MediaItem.Builder().setUri(s.uri).setMediaId(s.id.toString()).setMediaMetadata(md).build()
        val insertAt = (exo.currentMediaItemIndex + 1).coerceIn(0, exo.mediaItemCount)
        exo.addMediaItem(insertAt, mi)
        push()
    }

    fun removeFromQueue(index: Int) {
        if (index in 0 until exo.mediaItemCount) {
            exo.removeMediaItem(index)
            push()
        }
    }

    fun moveQueue(from: Int, to: Int) {
        exo.moveMediaItem(from, to)
        push()
    }

    fun clearQueue() {
        exo.clearMediaItems()
        exo.stop()
        push()
    }

    fun stopAndHide() {
        exo.stop()
        exo.clearMediaItems()
        push()
    }

    fun queue(): List<Long> {
        return (0 until exo.mediaItemCount).mapNotNull { i ->
            exo.getMediaItemAt(i).mediaId.toLongOrNull()
        }
    }

    fun release() {
        posJob?.cancel()
        mediaSession?.release()
        mediaSession = null
        exo.release()
    }
}
