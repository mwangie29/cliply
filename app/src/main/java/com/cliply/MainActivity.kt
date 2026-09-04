package com.cliply

import android.Manifest
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
import androidx.navigation.compose.*
import com.cliply.core.ui.components.*
import com.cliply.core.ui.theme.*
import com.cliply.download.ui.DownloadsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { CliplyTheme { CliplyApp() } } } }

@Composable fun CliplyApp() {
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) { if (Build.VERSION.SDK_INT >= 33) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
    val nav = rememberNavController()
    val current = nav.currentBackStackEntryAsState().value?.destination?.route ?: "home"
    Scaffold(bottomBar = { CliplyBottomNavigation(current.replaceFirstChar { it.uppercase() }) { nav.navigate(it.lowercase()) { launchSingleTop = true } } }, containerColor = CliplyBackground) { padding ->
        NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") { HomeScreen(onDownload = { nav.navigate("download") }) }
            composable("download") { DownloadScreen() }
            composable("downloads") { DownloadsScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

@Composable fun HomeScreen(onDownload: () -> Unit) { var url by remember { mutableStateOf("") }; LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { item { Text("Cliply", style = MaterialTheme.typography.titleLarge) }; item { Spacer(Modifier.height(24.dp)); Text("Share it.", style = MaterialTheme.typography.displaySmall, color = CliplyPrimary); Text("Keep scrolling.", style = MaterialTheme.typography.displaySmall); Text("Save videos without leaving the app you're using.", color = CliplyTextSecondary, modifier = Modifier.padding(top = 8.dp)) }; item { CliplyTextField(url, { url = it }, "Paste a video link", Modifier.fillMaxWidth()); CliplyButton("Download", onDownload, Modifier.fillMaxWidth().padding(top = 12.dp)) }; item { Text("Recent downloads", style = MaterialTheme.typography.titleMedium) }; items(listOf("Instagram  ·  Today  ·  20.1 MB", "TikTok  ·  Yesterday  ·  14.8 MB", "Facebook  ·  Earlier  ·  31.2 MB")) { text -> CliplyCard(Modifier.fillMaxWidth()) { Text(text, Modifier.padding(18.dp)) } } } }

@Composable fun DownloadScreen() { var state by remember { mutableStateOf("Link detected") }; Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text(state, style = MaterialTheme.typography.headlineMedium); Text(if (state == "Link detected") "We found a video you can download." else if (state == "Downloading") "Instagram video" else "Your video is saved to your device.", color = CliplyTextSecondary); CliplyCard(Modifier.fillMaxWidth().height(220.dp)) { Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { Text("VIDEO PREVIEW", color = CliplyTextTertiary) } }; when (state) { "Link detected" -> { Text("Best available quality", color = CliplyTextSecondary); CliplyButton("Download", { state = "Downloading" }, Modifier.fillMaxWidth()) }; "Downloading" -> { Text("12.4 MB / 20.1 MB                 62%"); CliplyProgressBar(0.62f, Modifier.fillMaxWidth()); Text("2.8 MB/s", color = CliplyTextSecondary); CliplyOutlinedButton("Cancel", { state = "Link detected" }, Modifier.fillMaxWidth()) }; else -> { Text("20.1 MB", color = CliplyTextSecondary); CliplyButton("Open", {}, Modifier.fillMaxWidth()); CliplyOutlinedButton("Share", {}, Modifier.fillMaxWidth()) } } } }

@Composable fun DownloadsScreen(viewModel: DownloadsViewModel = hiltViewModel()) { val jobs by viewModel.jobs.collectAsState(); LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { Text("Downloads", style = MaterialTheme.typography.headlineMedium) }; if (jobs.isEmpty()) item { Text("No downloads yet", color = CliplyTextSecondary) } else { item { Text("Recent", style = MaterialTheme.typography.titleMedium) }; items(jobs) { job -> CliplyCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text(job.title ?: "Cliply download"); Text("${job.platform.name}  ·  ${job.status.name}  ·  ${formatBytes(job.totalBytes)}", color = CliplyTextSecondary); job.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) } } } } } } }

private fun formatBytes(value: Long): String = if (value <= 0) "size pending" else "%.1f MB".format(value / 1_000_000.0)

@Composable fun SettingsScreen() { LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { item { Text("Settings", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(12.dp)) }; item { SettingsSection("Downloads", listOf("Download quality                 Best available", "Save location                         Device", "Wi-Fi only                              Off", "Simultaneous downloads         2")) }; item { SettingsSection("Notifications", listOf("Download notifications           On", "Sound                                      On", "Vibration                                Off")) }; item { SettingsSection("General", listOf("Auto-detect links                  On", "Theme                                     Dark", "Language                                English")) }; item { SettingsSection("Account", listOf("Cliply account                        Not signed in", "Cliply Pro                               Upgrade")) }; item { SettingsSection("About", listOf("Terms", "Privacy", "About Cliply")) } } }

@Composable private fun SettingsSection(title: String, rows: List<String>) { Text(title, style = MaterialTheme.typography.titleMedium, color = CliplyTextSecondary, modifier = Modifier.padding(top = 12.dp)); CliplyCard(Modifier.fillMaxWidth()) { rows.forEach { Text(it, Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) } } }
