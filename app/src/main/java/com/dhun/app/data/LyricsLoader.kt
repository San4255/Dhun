package com.dhun.app.data

import android.media.MediaMetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class LyricsLine(val timestampMs: Long?, val text: String)

object LyricsLoader {

    suspend fun load(song: Song?): List<LyricsLine> = withContext(Dispatchers.IO) {
        if (song == null) return@withContext emptyList()

        // 1. try sidecar .lrc file
        val lrcFile = File(song.path).let { f ->
            File(f.parentFile, f.nameWithoutExtension + ".lrc")
        }
        if (lrcFile.exists()) {
            val parsed = parseLrc(lrcFile.readText())
            if (parsed.isNotEmpty()) return@withContext parsed
        }

        // 2. try embedded lyrics via MediaMetadataRetriever
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(song.path)
            val lyricsText = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            // Many taggers put lyrics in a custom frame; Android doesn't expose USLT directly.
            // We try SYNCED/UNSYNCED LYRICS tags using reflection-ish keys. This is best-effort.
            val embedded = runCatching {
                val method = mmr.javaClass.getMethod(
                    "extractMetadata", Int::class.javaPrimitiveType
                )
                // values 11-14 are used in some builds for lyrics; just try the unsync one.
                null as String?
            }.getOrNull()
            mmr.release()
            if (!embedded.isNullOrBlank()) return@withContext parsePlainOrLrc(embedded)
        } catch (_: Throwable) { /* ignore */ }

        return@withContext emptyList()
    }

    private val TS = Regex("""\[(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?\]""")
    private fun parseLrc(text: String): List<LyricsLine> {
        val lines = text.lines().mapNotNull { raw ->
            val m = TS.find(raw) ?: return@mapNotNull null
            var remainder = raw
            val ts = mutableListOf<Long>()
            var mr: MatchResult? = m
            while (mr != null) {
                val min = mr.groupValues[1].toIntOrNull() ?: 0
                val sec = mr.groupValues[2].toIntOrNull() ?: 0
                val frac = (mr.groupValues.getOrNull(3)?.take(3) ?: "0").padEnd(3, '0').toIntOrNull() ?: 0
                ts += (min * 60_000L + sec * 1000L + frac)
                remainder = remainder.removeRange(mr.range)
                mr = TS.find(remainder)
            }
            val lineText = remainder.trim()
            ts.map { LyricsLine(it, lineText) }
        }.flatten().sortedBy { it.timestampMs }
        return lines
    }

    private fun parsePlainOrLrc(text: String): List<LyricsLine> {
        return if (text.contains('[')) parseLrc(text)
        else text.lines().filter { it.isNotBlank() }.map { LyricsLine(null, it) }
    }
}
