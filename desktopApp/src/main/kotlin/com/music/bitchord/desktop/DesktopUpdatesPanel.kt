package com.music.bitchord.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal object DesktopUpdateCenter {
    val open = MutableStateFlow(false)
    fun show() { open.value = true }
}

@Composable
internal fun DesktopUpdatesPanel() {
    if (!DesktopPlatform.isMac) { LegacyDesktopUpdateNotice(); return }
    val open by DesktopUpdateCenter.open.collectAsState()
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var showNotes by remember { mutableStateOf(false) }
    var latest by remember { mutableStateOf<DesktopUpdateChecker.UpdateInfo?>(null) }
    var problem by remember { mutableStateOf<String?>(null) }
    var checkedAt by remember { mutableStateOf<Instant?>(null) }
    var downloading by remember { mutableStateOf(false) }
    var downloaded by remember { mutableLongStateOf(0) }
    var installer by remember { mutableStateOf<Path?>(null) }
    var downloadJob by remember { mutableStateOf<Job?>(null) }
    fun check(auto: Boolean = false) {
        if (checking) return
        checking = true
        scope.launch {
            try {
                val release = DesktopUpdateChecker.latest()
                if (latest?.downloadUrl != release.downloadUrl) { installer = null; downloaded = 0; showNotes = false }
                latest = release
                checkedAt = Instant.now()
                problem = null
                if (auto && DesktopUpdateChecker.isNewer(release.version, DesktopUpdateChecker.currentVersion)) {
                    DesktopUpdateCenter.show()
                }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { problem = "Could not check updates. Check your connection and try again." }
            finally { checking = false }
        }
    }
    LaunchedEffect(Unit) { check(auto = true) }
    LaunchedEffect(open) { if (open && latest == null && !checking) check() }
    if (!open) return
    val update = latest
    val newer = update != null && DesktopUpdateChecker.isNewer(update.version, DesktopUpdateChecker.currentVersion)
    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF0A84FF))) {
        AlertDialog(
            onDismissRequest = { DesktopUpdateCenter.open.value = false },
            title = { Text("Version & Updates") },
            text = {
                Column(Modifier.widthIn(min = 340.dp, max = 460.dp).heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Installed", style = MaterialTheme.typography.titleSmall)
                    Text("Version ${DesktopUpdateChecker.currentVersion.replace("-build", " (build ")}".let {
                        if ("(build " in it) "$it)" else it
                    })
                    Text("this version is created by Chris Tambayong - Fumakill4")
                    Text("Copyright © 2026 BitChord contributors", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider()
                    Text("Latest published release", style = MaterialTheme.typography.titleSmall)
                    if (update != null) {
                        Text(update.version.replace("-build", " build "))
                        update.publishedAt?.let { Text("Published: ${it.substringBefore('T')}") }
                        Text(when {
                            newer -> "An update is available."
                            DesktopUpdateChecker.isNewer(DesktopUpdateChecker.currentVersion, update.version) ->
                                "Your installed build is newer than the published release."
                            else -> "You have the latest published build."
                        })
                        update.notes?.takeIf { it.isNotBlank() }?.let {
                            TextButton(onClick = { showNotes = !showNotes }) { Text(if (showNotes) "Hide What’s New" else "What’s New") }
                            if (showNotes) Text(it.take(3000), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    checkedAt?.let {
                        Text("Last successful check: " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                            .withZone(ZoneId.systemDefault()).format(it), style = MaterialTheme.typography.bodySmall)
                    }
                    problem?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (checking) LinearProgressIndicator(Modifier.fillMaxWidth())
                    TextButton(onClick = { check() }, enabled = !checking && !downloading) {
                        Text(if (checking) "Checking…" else "Check for Updates")
                    }
                    if (downloading) {
                        val size = update?.assetSize ?: 1L
                        LinearProgressIndicator(progress = { (downloaded.toFloat() / size).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                        Text("Downloading: ${downloaded / 1_048_576} / ${size / 1_048_576} MB")
                        TextButton(onClick = { downloadJob?.cancel() }) { Text("Cancel download") }
                    } else if (newer && installer == null && update.downloadUrl != null) {
                        Button(onClick = {
                            downloading = true
                            downloaded = 0
                            problem = null
                            downloadJob = scope.launch {
                                try {
                                    installer = DesktopUpdateDownload.download(update, Path.of(System.getProperty("user.home"), "Downloads")) { done, _ ->
                                        downloaded = done
                                    }
                                } catch (e: CancellationException) { problem = "Download cancelled."; throw e }
                                catch (_: Exception) { problem = "The installer could not be downloaded and verified. Please try again." }
                                finally { downloading = false }
                            }
                        }) { Text("Download Update") }
                    }
                    installer?.let { file ->
                        Text("Verified installer saved in Downloads: ${file.fileName}")
                        Button(onClick = {
                            runCatching { Desktop.getDesktop().open(file.toFile()) }
                                .onFailure { problem = "Could not open the installer. Open it from Downloads." }
                        }) { Text("Open Installer") }
                        Text("Open the DMG and drag BitChord to Applications to finish updating.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { DesktopUpdateCenter.open.value = false }) { Text("Done") } },
        )
    }
}

/** Preserve the existing browser-based update flow on platforms outside this macOS change. */
@Composable
private fun LegacyDesktopUpdateNotice() {
    var available by remember { mutableStateOf<DesktopUpdateChecker.UpdateInfo?>(null) }
    LaunchedEffect(Unit) { available = DesktopUpdateChecker.check() }
    available?.let { update ->
        AlertDialog(onDismissRequest = { available = null },
            title = { Text("Update available") },
            text = { Text("BitChord ${update.version} is available." + (update.notes?.take(600)?.let { "\n\n$it" } ?: "")) },
            confirmButton = { TextButton(onClick = {
                DesktopExternalLinks.open(update.downloadUrl ?: update.releaseUrl)
                available = null
            }) { Text("Download") } },
            dismissButton = { TextButton(onClick = { available = null }) { Text("Later") } })
    }
}
