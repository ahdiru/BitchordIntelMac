package com.music.bitchord.desktop

// This App was build by Chris Tambayong - Fumakill4

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Looks at the repo's "latest release" on GitHub once per launch and says whether it is newer
 * than the running build. The installer is not run from here: the dialog opens the asset in the
 * application update panel, which downloads and verifies the installer before it can be opened.
 *
 * GitHub's "latest" skips pre-releases and drafts, so a `-beta` desktop build is only ever
 * nudged toward a published release.
 */
internal object DesktopUpdateChecker {

    data class UpdateInfo(
        val version: String,
        val releaseUrl: String,
        /** The installer for this platform, or null when the release has none. */
        val downloadUrl: String?,
        val notes: String?,
        val assetName: String? = null,
        val assetSize: Long? = null,
        val sha256: String? = null,
        val publishedAt: String? = null,
    )

    private val latestReleaseUrl = if (DesktopPlatform.isMac)
        "https://api.github.com/repos/fumakill4/BitChord/releases?per_page=30"
    else "https://api.github.com/repos/kushagrasinghx/BitChord/releases/latest"

    private val macArm64: Boolean
        get() = System.getProperty("os.arch").orEmpty().lowercase() in setOf("aarch64", "arm64")

    val currentVersion: String = System.getProperty("bitchord.version") ?: "1.8-beta1"

    private val json = Json { ignoreUnknownKeys = true }

    private val http by lazy {
        HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
            }
        }
    }

    suspend fun check(): UpdateInfo? = runCatching {
        latest().takeIf { isNewer(it.version, currentVersion) }
    }.getOrNull()

    /** Manual checks report network errors instead of claiming the installed version is latest. */
    suspend fun latest(): UpdateInfo {
        val response = http.get(latestReleaseUrl) { header("Accept", "application/vnd.github+json") }
        check(response.status.isSuccess()) { "Could not check updates (HTTP ${response.status.value})." }
        val payload = json.parseToJsonElement(response.bodyAsText())
        val release = if (DesktopPlatform.isMac) (payload as? JsonArray)?.let { selectMacRelease(it, macArm64) }
            else payload as? JsonObject
        requireNotNull(release) { "No published installer is available for this Mac." }
        val tag = requireNotNull(release["tag_name"]?.jsonPrimitive?.contentOrNull)
        val url = requireNotNull(release["html_url"]?.jsonPrimitive?.contentOrNull)
        val download = installerUrl(release)
        val asset = release["assets"]?.jsonArray?.mapNotNull { it as? JsonObject }?.firstOrNull {
            it["browser_download_url"]?.jsonPrimitive?.contentOrNull == download
        }
        return UpdateInfo(
            version = if (DesktopPlatform.isMac) macReleaseVersion(release, macArm64) else releaseVersion(tag), releaseUrl = url, downloadUrl = download,
            notes = release["body"]?.jsonPrimitive?.contentOrNull,
            assetName = asset?.get("name")?.jsonPrimitive?.contentOrNull,
            assetSize = asset?.get("size")?.jsonPrimitive?.longOrNull,
            sha256 = asset?.get("digest")?.jsonPrimitive?.contentOrNull?.removePrefix("sha256:"),
            publishedAt = release["published_at"]?.jsonPrimitive?.contentOrNull,
        )
    }

    /**
     * The release names its files `BitChord-<version>-windows-x64-setup.exe` and
     * `BitChord-<version>-linux-x86_64.AppImage` (see .github/workflows/release.yml); match on the
     * platform part so a version change does not matter.
     */
    private fun installerUrl(release: JsonObject): String? {
        if (DesktopPlatform.isMac) return macInstallerUrl(release, macArm64)
        val suffixes = when {
            DesktopPlatform.isWindows -> listOf("-windows-x64-setup.exe")
            DesktopPlatform.isLinux -> listOf(".AppImage", "-linux-amd64.deb")
            else -> return null
        }
        val assets = release["assets"]?.jsonArray?.mapNotNull { it as? JsonObject }.orEmpty()
        for (suffix in suffixes) {
            val hit = assets.firstOrNull { asset ->
                asset["name"]?.jsonPrimitive?.contentOrNull?.endsWith(suffix, ignoreCase = true) == true &&
                    asset["state"]?.jsonPrimitive?.contentOrNull == "uploaded"
            }
            hit?.get("browser_download_url")?.jsonPrimitive?.contentOrNull?.let { return it }
        }
        return null
    }

    internal fun releaseVersion(tag: String): String = tag.removePrefix("macos-").removePrefix("v")

    internal fun macInstallerUrl(release: JsonObject, arm64: Boolean): String? {
        val suffix = if (arm64) "-macos-arm64.dmg" else "-macos-x64.dmg"
        return release["assets"]?.jsonArray?.mapNotNull { it as? JsonObject }?.firstOrNull {
            it["name"]?.jsonPrimitive?.contentOrNull?.endsWith(suffix, ignoreCase = true) == true &&
                it["state"]?.jsonPrimitive?.contentOrNull == "uploaded"
        }?.get("browser_download_url")?.jsonPrimitive?.contentOrNull
    }

    internal fun macReleaseVersion(release: JsonObject, arm64: Boolean): String {
        val download = macInstallerUrl(release, arm64)
        val name = release["assets"]?.jsonArray?.mapNotNull { it as? JsonObject }?.firstOrNull {
            it["browser_download_url"]?.jsonPrimitive?.contentOrNull == download
        }?.get("name")?.jsonPrimitive?.contentOrNull.orEmpty()
        val assetVersion = Regex("^BitChord-v?([0-9]+[.][0-9]+[.][0-9]+(?:[.]E[0-9]+|-build[0-9]+)?)-macos-(?:arm64|x64)[.]dmg$")
            .find(name)?.groupValues?.get(1)
        return assetVersion ?: releaseVersion(release["tag_name"]?.jsonPrimitive?.contentOrNull.orEmpty())
    }

    /** Assets may be replaced on an older release; compare their build, not release-list order. */
    internal fun selectMacRelease(releases: JsonArray, arm64: Boolean): JsonObject? =
        releases.mapNotNull { it as? JsonObject }.filter {
            it["draft"]?.jsonPrimitive?.booleanOrNull != true &&
                it["prerelease"]?.jsonPrimitive?.booleanOrNull != true && macInstallerUrl(it, arm64) != null
        }.maxWithOrNull(Comparator { a, b ->
            val av = macReleaseVersion(a, arm64)
            val bv = macReleaseVersion(b, arm64)
            when { isNewer(av, bv) -> 1; isNewer(bv, av) -> -1; else -> 0 }
        })

    private class Parsed(val parts: List<Int>, val preRelease: Boolean, val edit: Int)

    private fun parse(raw: String): Parsed {
        val dash = raw.indexOf('-')
        val base = if (dash >= 0) raw.substring(0, dash) else raw
        val build = Regex("-build([0-9]+)$").find(raw)?.groupValues?.get(1)?.toIntOrNull()
        val edit = build ?: Regex("[.]E([0-9]+)$").find(base)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val numeric = base.substringBefore(".E")
        return Parsed(numeric.split('.').map { it.toIntOrNull() ?: 0 }, dash >= 0 && build == null, edit)
    }

    /** Numeric comparison, with a `-betaN` build counted as older than the plain release it leads up to. */
    internal fun isNewer(latest: String, current: String): Boolean {
        val l = parse(latest)
        val c = parse(current)
        for (i in 0 until maxOf(l.parts.size, c.parts.size)) {
            val a = l.parts.getOrElse(i) { 0 }
            val b = c.parts.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        if (l.edit != c.edit) return l.edit > c.edit
        return c.preRelease && !l.preRelease
    }
}
