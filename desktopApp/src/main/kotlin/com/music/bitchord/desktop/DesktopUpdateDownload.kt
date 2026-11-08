package com.music.bitchord.desktop

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.net.URI
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.security.MessageDigest

internal object DesktopUpdateDownload {
    private val http by lazy {
        HttpClient(CIO) {
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 30_000
                requestTimeoutMillis = 3_600_000
            }
        }
    }

    suspend fun download(info: DesktopUpdateChecker.UpdateInfo, directory: Path,
                         progress: (Long, Long) -> Unit): Path {
        val uri = URI(requireNotNull(info.downloadUrl) { "No installer is available." })
        require(uri.scheme == "https" && uri.host == "github.com" &&
            uri.path.startsWith("/fumakill4/BitChord/releases/download/")) { "Unexpected installer source." }
        return http.prepareGet(uri.toString()).execute { response ->
            check(response.status.isSuccess()) { "Download failed (HTTP ${response.status.value})." }
            val channel = response.bodyAsChannel()
            saveVerified(info, directory, { buffer -> channel.readAvailable(buffer) }, progress)
        }
    }

    /** Stream into a private partial file; only a complete, verified download receives its final name. */
    internal suspend fun saveVerified(info: DesktopUpdateChecker.UpdateInfo, directory: Path,
                                     read: suspend (ByteArray) -> Int,
                                     progress: (Long, Long) -> Unit = { _, _ -> }): Path = withContext(Dispatchers.IO) {
        val name = requireNotNull(info.assetName)
        require(name == Path.of(name).fileName.toString() && !name.contains('\\') && name.endsWith(".dmg")) {
            "Invalid installer name."
        }
        val size = requireNotNull(info.assetSize).also { require(it > 0) }
        val expected = requireNotNull(info.sha256) { "The published installer has no checksum." }.lowercase()
        require(expected.matches(Regex("[0-9a-f]{64}"))) { "Invalid published checksum." }
        Files.createDirectories(directory)
        val partial = Files.createTempFile(directory, ".bitchord-update-", ".part")
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            var total = 0L
            var lastProgress = 0L
            Files.newOutputStream(partial).use { output ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    currentCoroutineContext().ensureActive()
                    val count = read(buffer)
                    if (count < 0) break
                    if (count == 0) continue
                    total += count
                    check(total <= size) { "Installer size differs from the published release." }
                    output.write(buffer, 0, count)
                    digest.update(buffer, 0, count)
                    val now = System.nanoTime()
                    if (now - lastProgress >= 100_000_000 || total == size) {
                        progress(total, size)
                        lastProgress = now
                    }
                }
            }
            check(total == size) { "The download was incomplete. Try again." }
            val actual = digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
            check(actual == expected) { "Installer checksum did not match. Please download again." }
            currentCoroutineContext().ensureActive()
            // Never replace another installer or follow a pre-existing symlink.
            var target = directory.resolve(name)
            var suffix = 1
            while (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                target = directory.resolve(name.removeSuffix(".dmg") + " (${suffix++}).dmg")
            }
            Files.move(partial, target)
        } finally {
            Files.deleteIfExists(partial)
        }
    }
}
