package com.dhun.app.data

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val artistId: Long?,
    val path: String,
    val durationMs: Long,
    val bitrate: Int = 0,
    val format: String = "mp3",
    val sizeBytes: Long = 0,
    val dateAdded: Long = 0L,
    val trackNumber: Int = 0,
    val discNumber: Int = 0,
    val year: Int? = null,
    val uri: Uri,
    val albumArtUri: Uri? = null,
) {
    val displayDuration: String get() = formatDuration(durationMs)
    val bitrateLabel: String get() = if (bitrate > 0) "${bitrate / 1000}kbps" else ""
}

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val artistId: Long?,
    val year: Int?,
    val trackCount: Int,
    val durationMs: Long,
    val artUri: Uri?,
    val songIds: List<Long>,
)

data class Artist(
    val id: Long,
    val name: String,
    val trackCount: Int,
    val albumCount: Int,
)

data class Folder(
    val path: String,
    val name: String,
    val trackCount: Int,
)

data class Playlist(
    val id: Long,
    val name: String,
    val songIds: MutableList<Long> = mutableListOf(),
    val isSmart: Boolean = false,
)

fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val total = ms / 1000
    val m = total / 60
    val s = total % 60
    return if (m >= 60) {
        val h = m / 60
        val rm = m % 60
        "%d:%02d:%02d".format(h, rm, s)
    } else {
        "%d:%02d".format(m, s)
    }
}

fun formatBitrateKbps(bps: Int): String = if (bps > 0) "${bps / 1000}kbps" else ""
fun extToFormat(ext: String?): String = when (ext?.lowercase()) {
    "mp3" -> "mp3"; "flac" -> "flac"; "wav" -> "wav"
    "m4a" -> "m4a"; "aac" -> "m4a"; "ogg" -> "ogg"; "opus" -> "opus"
    else -> ext?.lowercase() ?: "mp3"
}
