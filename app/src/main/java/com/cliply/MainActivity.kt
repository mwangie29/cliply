package com.cliply

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.cliply.core.ui.components.*
import com.cliply.core.ui.theme.*
import com.cliply.download.ui.DownloadJobViewModel
import com.cliply.download.ui.DownloadsViewModel
import com.cliply.download.ui.HomeViewModel
import com.cliply.domain.model.DownloadJob
import com.cliply.domain.model.DownloadStatus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { CliplyTheme { CliplyApp() } } } }

@Composable fun CliplyApp() {
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) { if (Build.VERSION.SDK_INT >= 33) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
    val nav = rememberNavController()
    val current = nav.currentBackStackEntryAsState().value?.destination?.route ?: "home"
    Scaffold(bottomBar = { CliplyBottomNavigation(current.substringBefore('/').replaceFirstChar { it.uppercase() }) { nav.navigate(it.lowercase()) { launchSingleTop = true } } }, containerColor = CliplyBackground) { padding ->
        NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") { HomeScreen(onDownload = { jobId -> nav.navigate("download/$jobId") }) }
            composable("download/{jobId}", arguments = listOf(navArgument("jobId") { type = NavType.StringType })) { DownloadScreen() }
            composable("downloads") { DownloadsScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

@Composable fun HomeScreen(onDownload: (String) -> Unit, viewModel: HomeViewModel = hiltViewModel()) { var url by remember { mutableStateOf("https://filesamples.com/samples/video/mp4/sample_640x360.mp4") }; val message by viewModel.message.collectAsState(); LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { item { Text("Cliply", style = MaterialTheme.typography.titleLarge) }; item { Spacer(Modifier.height(24.dp)); Text("Share it.", style = MaterialTheme.typography.displaySmall, color = CliplyPrimary); Text("Keep scrolling.", style = MaterialTheme.typography.displaySmall); Text("Save videos without leaving the app you're using.", color = CliplyTextSecondary, modifier = Modifier.padding(top = 8.dp)) }; item { CliplyTextField(url, { url = it }, "Paste a video link", Modifier.fillMaxWidth()); CliplyButton("Download", { viewModel.startDownload(url, onDownload) }, Modifier.fillMaxWidth().padding(top = 12.dp)); message?.let { Text(it, color = CliplyTextSecondary, modifier = Modifier.padding(top = 8.dp)) } }; item { Text("Recent downloads", style = MaterialTheme.typography.titleMedium) } } }

@Composable fun DownloadScreen(viewModel: DownloadJobViewModel = hiltViewModel()) { val job by viewModel.job.collectAsState(); val context = androidx.compose.ui.platform.LocalContext.current; Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text(job?.status?.displayName() ?: "Preparing download", style = MaterialTheme.typography.headlineMedium); Text(job?.title ?: "Resolving media", color = CliplyTextSecondary); CliplyCard(Modifier.fillMaxWidth().height(180.dp)) { Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { Text(job?.mimeType ?: "MEDIA", color = CliplyTextTertiary) } }; if (job != null) { Text("${formatBytes(job!!.downloadedBytes)} / ${formatBytes(job!!.totalBytes)}", color = CliplyTextSecondary); CliplyProgressBar((job!!.progressPercent.coerceIn(0, 100) / 100f), Modifier.fillMaxWidth()); when (job!!.status) { DownloadStatus.DOWNLOADING, DownloadStatus.RESOLVING, DownloadStatus.QUEUED -> CliplyOutlinedButton("Cancel", { viewModel.cancel() }, Modifier.fillMaxWidth()); DownloadStatus.COMPLETED -> { Text("Saved to device", color = CliplyTextSecondary); if (job!!.mediaStoreUri != null) { CliplyButton("Open", { openMedia(context, job!!) }, Modifier.fillMaxWidth()); CliplyOutlinedButton("Share", { shareMedia(context, job!!) }, Modifier.fillMaxWidth()) } }; DownloadStatus.FAILED -> Text(job!!.errorMessage ?: "Download failed", color = MaterialTheme.colorScheme.error); DownloadStatus.CANCELLED -> Text("Download cancelled", color = CliplyTextSecondary); else -> Unit } } } }

@Composable fun DownloadsScreen(viewModel: DownloadsViewModel = hiltViewModel()) { val jobs by viewModel.jobs.collectAsState(); val context = androidx.compose.ui.platform.LocalContext.current; LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { Text("Downloads", style = MaterialTheme.typography.headlineMedium) }; if (jobs.isEmpty()) item { Text("No downloads yet", color = CliplyTextSecondary) } else { item { Text("Recent", style = MaterialTheme.typography.titleMedium) }; items(jobs) { job -> DownloadHistoryCard(job, context) } } } }

@Composable private fun DownloadHistoryCard(job: DownloadJob, context: Context) { CliplyCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(job.title ?: "Cliply download"); Text("${job.platform.name}  ·  ${job.status.name}  ·  ${formatBytes(job.totalBytes)}", color = CliplyTextSecondary); job.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }; if (job.status == DownloadStatus.COMPLETED && job.mediaStoreUri != null) { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TextButton(onClick = { openMedia(context, job) }) { Text("Open") }; TextButton(onClick = { shareMedia(context, job) }) { Text("Share") } } } } } }

private fun DownloadStatus.displayName() = when (this) { DownloadStatus.RESOLVING -> "Resolving media"; DownloadStatus.QUEUED -> "Queued"; DownloadStatus.DOWNLOADING -> "Downloading"; DownloadStatus.COMPLETED -> "Download complete"; DownloadStatus.FAILED -> "Download failed"; DownloadStatus.CANCELLED -> "Download cancelled"; else -> name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() } }
private fun openMedia(context: Context, job: DownloadJob) { val uri = android.net.Uri.parse(job.mediaStoreUri); val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, job.mimeType ?: "video/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); runCatching { context.startActivity(intent) }.onFailure { android.widget.Toast.makeText(context, "No compatible app can open this media", android.widget.Toast.LENGTH_SHORT).show() } }
private fun shareMedia(context: Context, job: DownloadJob) { val uri = android.net.Uri.parse(job.mediaStoreUri); val intent = Intent.createChooser(Intent(Intent.ACTION_SEND).setType(job.mimeType ?: "video/*").putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), "Share media"); runCatching { context.startActivity(intent) }.onFailure { android.widget.Toast.makeText(context, "No compatible sharing app found", android.widget.Toast.LENGTH_SHORT).show() } }
private fun formatBytes(value: Long): String = if (value <= 0) "size pending" else "%.1f MB".format(value / 1_000_000.0)

@Composable fun SettingsScreen() { LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { item { Text("Settings", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(12.dp)) }; item { InstagramAccountCard() }; item { SettingsSection("Downloads", listOf("Download quality                 Best available", "Save location                         Device", "Wi-Fi only                              Off", "Simultaneous downloads         2")) }; item { SettingsSection("Notifications", listOf("Download notifications           On", "Sound                                      On", "Vibration                                Off")) }; item { SettingsSection("General", listOf("Auto-detect links                  On", "Theme                                     Dark", "Language                                English")) }; item { SettingsSection("Account", listOf("Cliply account                        Not signed in", "Cliply Pro                               Upgrade")) }; item { SettingsSection("About", listOf("Terms", "Privacy", "About Cliply")) } } }

@Composable private fun InstagramAccountCard() { Text("Instagram", style = MaterialTheme.typography.titleMedium, color = CliplyTextSecondary, modifier = Modifier.padding(top = 12.dp)); CliplyCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Authorized account connection", style = MaterialTheme.typography.titleLarge); Text("Not connected", color = CliplyTextSecondary); Text("Cliply will support only professional accounts connected through Meta's official authorization flow. Public visibility alone does not grant download access.", color = CliplyTextSecondary); Text("Instagram integration is not enabled yet. Direct HTTPS media downloads remain available.", color = CliplyTextTertiary); CliplyOutlinedButton("Connect Instagram account", { }, Modifier.fillMaxWidth(), enabled = false) } } }

@Composable private fun SettingsSection(title: String, rows: List<String>) { Text(title, style = MaterialTheme.typography.titleMedium, color = CliplyTextSecondary, modifier = Modifier.padding(top = 12.dp)); CliplyCard(Modifier.fillMaxWidth()) { rows.forEach { Text(it, Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) } } }
