package com.music.bitchord.desktop

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.test.*

class DesktopUpdateDownloadTest {
    private val bytes = ByteArray(180_000) { (it % 251).toByte() }
    private fun info() = DesktopUpdateChecker.UpdateInfo("1.9.1-build31", "https://github.com/fumakill4/BitChord/releases",
        "https://github.com/fumakill4/BitChord/releases/download/macos-v1.9.1-build31/BitChord-macos-arm64.dmg", null,
        "BitChord-macos-arm64.dmg", bytes.size.toLong(),
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) })

    private suspend fun save(info: DesktopUpdateChecker.UpdateInfo, dir: Path, data: ByteArray = bytes): Path {
        var offset = 0
        return DesktopUpdateDownload.saveVerified(info, dir, { buffer ->
            if (offset == data.size) -1 else {
                val count = minOf(1703, data.size - offset, buffer.size)
                data.copyInto(buffer, 0, offset, offset + count)
                offset += count
                count
            }
        })
    }

    private fun files(dir: Path): List<Path> = Files.list(dir).use { it.toList() }
    private fun cleanup(dir: Path) { files(dir).forEach(Files::delete); Files.delete(dir) }

    @Test fun streamedInstallerOnlyReceivesItsFinalNameAfterVerification() = runBlocking {
        val dir = Files.createTempDirectory("bitchord-update-test-")
        try {
            val file = save(info(), dir)
            assertContentEquals(bytes, Files.readAllBytes(file))
            assertEquals(listOf(file), files(dir))
        } finally { cleanup(dir) }
    }

    @Test fun corruptAndIncompleteDownloadsLeaveNoInstallerOrPartialFile() = runBlocking {
        val dir = Files.createTempDirectory("bitchord-update-test-")
        try {
            assertFailsWith<IllegalStateException> { save(info().copy(sha256 = "0".repeat(64)), dir) }
            assertTrue(files(dir).isEmpty())
            assertFailsWith<IllegalStateException> { save(info(), dir, bytes.copyOf(100)) }
            assertTrue(files(dir).isEmpty())
        } finally { cleanup(dir) }
    }

    @Test fun cancellationCleansUpTheOwnedPartialFile() = runBlocking {
        val dir = Files.createTempDirectory("bitchord-update-test-")
        try {
            assertFailsWith<CancellationException> {
                DesktopUpdateDownload.saveVerified(info(), dir, { throw CancellationException("cancel test") })
            }
            assertTrue(files(dir).isEmpty())
        } finally { cleanup(dir) }
    }

    @Test fun anExistingInstallerIsNeverReplaced() = runBlocking {
        val dir = Files.createTempDirectory("bitchord-update-test-")
        try {
            val original = dir.resolve(info().assetName!!)
            Files.writeString(original, "existing installer")
            val file = save(info(), dir)
            assertNotEquals(original, file)
            assertEquals("existing installer", Files.readString(original))
            assertContentEquals(bytes, Files.readAllBytes(file))
        } finally { cleanup(dir) }
    }

    @Test fun pathTraversalAndUnexpectedDownloadSourcesAreRejected() = runBlocking {
        val dir = Files.createTempDirectory("bitchord-update-test-")
        try {
            assertFailsWith<IllegalArgumentException> { save(info().copy(assetName = "../outside.dmg"), dir) }
            assertFailsWith<IllegalArgumentException> {
                DesktopUpdateDownload.download(info().copy(downloadUrl = "https://example.com/installer.dmg"), dir) { _, _ -> }
            }
            assertTrue(files(dir).isEmpty())
        } finally { cleanup(dir) }
    }
}
