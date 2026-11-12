package com.music.bitchord.desktop

import com.music.bitchord.data.lyrics.LyricLine
import com.music.bitchord.data.lyrics.LyricWord
import com.music.bitchord.data.model.Song
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopTouchBarLyricsTest {

    private val sampleSong = Song(
        videoId = "v1",
        title = "Bohemian Rhapsody",
        artist = "Queen",
        thumbnailUrl = null,
        fromAutoplay = false,
    )

    @Test
    fun `returns empty when no song is playing`() {
        val result = DesktopMacMedia.resolveLyric(
            song = null,
            lines = emptyList(),
            positionMs = 10_000L,
            lyricsOffsetMs = 0L,
            syncedLyricsEnabled = true,
        )
        assertEquals("", result.text)
        assertEquals(-1, result.revealedChars)
        assertFalse(result.isInstrumental)
    }

    @Test
    fun `falls back to track title and artist when lyrics are unavailable or unsynced`() {
        val unsynced = listOf(LyricLine(timeMs = 0L, text = "Plain line"))
        val result = DesktopMacMedia.resolveLyric(
            song = sampleSong,
            lines = unsynced,
            positionMs = 5_000L,
            lyricsOffsetMs = 0L,
            syncedLyricsEnabled = true,
        )
        assertEquals("Bohemian Rhapsody — Queen", result.text)
        assertEquals(-1, result.revealedChars)
        assertFalse(result.isInstrumental)
    }

    @Test
    fun `shows intro marker before first timestamp and instrumental marker on gaps`() {
        val lines = listOf(
            LyricLine(timeMs = 10_000L, text = "Is this the real life?"),
            LyricLine(timeMs = 15_000L, text = ""),
            LyricLine(timeMs = 20_000L, text = "Is this just fantasy?"),
        )

        val intro = DesktopMacMedia.resolveLyric(
            song = sampleSong,
            lines = lines,
            positionMs = 4_000L,
            lyricsOffsetMs = 0L,
            syncedLyricsEnabled = true,
        )
        assertEquals("♪ Bohemian Rhapsody — Queen", intro.text)
        assertTrue(intro.isInstrumental)

        val gap = DesktopMacMedia.resolveLyric(
            song = sampleSong,
            lines = lines,
            positionMs = 16_000L,
            lyricsOffsetMs = 0L,
            syncedLyricsEnabled = true,
        )
        assertEquals("♪ • • •", gap.text)
        assertTrue(gap.isInstrumental)
    }

    @Test
    fun `resolves line-synced lyrics with offset applied`() {
        val lines = listOf(
            LyricLine(timeMs = 10_000L, text = "First line"),
            LyricLine(timeMs = 15_000L, text = "Second line"),
        )
        val result = DesktopMacMedia.resolveLyric(
            song = sampleSong,
            lines = lines,
            positionMs = 14_600L,
            lyricsOffsetMs = 500L,
            syncedLyricsEnabled = true,
        )
        assertEquals("Second line", result.text)
        assertEquals(-1, result.revealedChars)
        assertFalse(result.isInstrumental)
    }

    @Test
    fun `resolves word-synced revealed character count`() {
        val line = LyricLine(
            timeMs = 10_000L,
            text = "Hello world",
            words = listOf(
                LyricWord(startMs = 10_000L, endMs = 11_000L, text = "Hello"),
                LyricWord(startMs = 11_000L, endMs = 12_000L, text = "world"),
            ),
        )
        val midFirstWord = DesktopMacMedia.resolveLyric(
            song = sampleSong,
            lines = listOf(line),
            positionMs = 10_500L,
            lyricsOffsetMs = 0L,
            syncedLyricsEnabled = true,
        )
        assertEquals("Hello world", midFirstWord.text)
        assertTrue(midFirstWord.revealedChars in 2..3)

        val afterLine = DesktopMacMedia.resolveLyric(
            song = sampleSong,
            lines = listOf(line),
            positionMs = 12_500L,
            lyricsOffsetMs = 0L,
            syncedLyricsEnabled = true,
        )
        assertEquals("Hello world".length, afterLine.revealedChars)
    }

    @Test
    fun `resolves Now Playing playback rate and clamps position for scrubber sync`() {
        assertEquals(0.0f, DesktopMacMedia.resolvePlaybackRate(isPlaying = false, awaitingAudio = false, playbackSpeed = 1.5f))
        assertEquals(0.0f, DesktopMacMedia.resolvePlaybackRate(isPlaying = true, awaitingAudio = true, playbackSpeed = 1.5f))
        assertEquals(1.5f, DesktopMacMedia.resolvePlaybackRate(isPlaying = true, awaitingAudio = false, playbackSpeed = 1.5f))

        assertEquals(0L, DesktopMacMedia.clampPositionMs(positionMs = -250L, durationMs = 180_000L))
        assertEquals(180_000L, DesktopMacMedia.clampPositionMs(positionMs = 200_000L, durationMs = 180_000L))
        assertEquals(42_000L, DesktopMacMedia.clampPositionMs(positionMs = 42_000L, durationMs = 0L))
    }
}
