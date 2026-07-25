package com.dhun.app.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

data class ScanStats(val scanned: Int, val totalFound: Int, val done: Boolean)

class LibraryRepository(private val ctx: Context) {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders.asStateFlow()

    private val _scanProgress = MutableStateFlow(ScanStats(0, 0, false))
    val scanProgress: StateFlow<ScanStats> = _scanProgress.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    // in-memory playlists (file-backed persistence stub)
    private val smartPlaylists = mutableListOf(
        Playlist(id = -1L, name = "recently added", isSmart = true),
        Playlist(id = -2L, name = "most played", isSmart = true),
        Playlist(id = -3L, name = "recently played", isSmart = true),
    )

    var lastScanTime: Long = 0L; private set

    suspend fun scan(minDurationSec: Int = 5) = withContext(Dispatchers.IO) {
        _scanProgress.value = ScanStats(0, 0, false)
        val list = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.IS_MUSIC,
        )
        val sel = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
        val args = arrayOf((minDurationSec * 1000).toString())
        val sort = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        ctx.contentResolver.query(collection, proj, sel, args, sort)?.use { c ->
            val total = c.count
            _scanProgress.value = ScanStats(0, total, false)
            val iId = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val iTitle = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val iArtist = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val iAlbum = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val iAlbumId = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val iArtistId = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val iData = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val iDur = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val iSize = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val iDate = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val iTrack = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val iMime = c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

            var n = 0
            while (c.moveToNext()) {
                val id = c.getLong(iId)
                val dataPath = c.getString(iData) ?: continue
                val dur = c.getLong(iDur)
                val ext = File(dataPath).extension
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                )
                val albumId = c.getLong(iAlbumId)
                val artUri = Uri.parse("content://media/external/audio/albumart/$albumId")
                val trackRaw = c.getInt(iTrack)
                val trackNo = trackRaw and 0xFFFF
                val song = Song(
                    id = id,
                    title = c.getString(iTitle) ?: File(dataPath).nameWithoutExtension,
                    artist = c.getString(iArtist)?.takeIf { it != "<unknown>" } ?: "unknown artist",
                    album = c.getString(iAlbum)?.takeIf { it != "<unknown>" } ?: "unknown album",
                    albumId = albumId,
                    artistId = c.getLong(iArtistId).takeIf { it > 0 },
                    path = dataPath,
                    durationMs = dur,
                    bitrate = estimateBitrate(c.getLong(iSize), dur),
                    format = extToFormat(ext),
                    sizeBytes = c.getLong(iSize),
                    dateAdded = c.getLong(iDate) * 1000L,
                    trackNumber = trackNo,
                    uri = uri,
                    albumArtUri = artUri,
                )
                list += song
                n++
                if (n % 25 == 0) _scanProgress.value = ScanStats(n, total, false)
            }
        }

        _songs.value = list
        buildAlbums()
        buildArtists()
        buildFolders()
        _playlists.value = smartPlaylists.toList()
        lastScanTime = System.currentTimeMillis()
        _scanProgress.value = ScanStats(list.size, list.size, true)
    }

    private fun estimateBitrate(size: Long, durMs: Long): Int {
        if (durMs <= 0 || size <= 0) return 0
        return ((size * 8L) / (durMs / 1000)).toInt()
    }

    private fun buildAlbums() {
        val grouped = _songs.value.groupBy { it.albumId }
        val list = grouped.map { (aid, songs) ->
            val first = songs.first()
            Album(
                id = aid,
                title = first.album,
                artist = songs.map { it.artist }.distinct().let {
                    if (it.size == 1) it[0] else "various artists"
                },
                artistId = first.artistId,
                year = first.year,
                trackCount = songs.size,
                durationMs = songs.sumOf { it.durationMs },
                artUri = first.albumArtUri,
                songIds = songs.sortedBy { it.trackNumber }.map { it.id },
            )
        }.sortedBy { it.title.lowercase() }
        _albums.value = list
    }

    private fun buildArtists() {
        val grouped = _songs.value.groupBy { it.artistId ?: it.artist.hashCode().toLong() }
        val list = grouped.map { (_, songs) ->
            Artist(
                id = songs.first().artistId ?: 0L,
                name = songs.first().artist,
                trackCount = songs.size,
                albumCount = songs.map { it.albumId }.distinct().size,
            )
        }.sortedBy { it.name.lowercase() }
        _artists.value = list
    }

    private fun buildFolders() {
        val grouped = _songs.value.groupBy { File(it.path).parent ?: "/" }
        val list = grouped.map { (p, songs) ->
            Folder(path = p, name = File(p).name.ifBlank { "/" }, trackCount = songs.size)
        }.sortedBy { it.name.lowercase() }
        _folders.value = list
    }

    fun findSong(id: Long?): Song? = id?.let { _songs.value.firstOrNull { s -> s.id == id } }
    fun songsForAlbum(albumId: Long): List<Song> = _songs.value.filter { it.albumId == albumId }.sortedBy { it.trackNumber }
    fun songsForArtist(artistName: String): List<Song> = _songs.value.filter { it.artist == artistName }.sortedBy { it.title.lowercase() }
    fun songsForFolder(path: String): List<Song> = _songs.value.filter { File(it.path).parent == path }.sortedBy { File(it.path).name.lowercase() }

    fun createPlaylist(name: String) {
        val id = (_playlists.value.maxOfOrNull { it.id } ?: 0L) + 1L
        _playlists.value = _playlists.value + Playlist(id = id, name = name)
    }

    fun deletePlaylist(id: Long) {
        _playlists.value = _playlists.value.filterNot { it.id == id && !it.isSmart }
    }

    fun addToPlaylist(playlistId: Long, songId: Long) {
        _playlists.value = _playlists.value.map { p ->
            if (p.id == playlistId && !p.songIds.contains(songId)) p.copy().also { it.songIds.add(songId) } else p
        }
    }
}
