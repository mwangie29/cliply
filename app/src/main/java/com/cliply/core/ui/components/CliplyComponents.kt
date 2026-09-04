package com.cliply.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cliply.core.ui.theme.*

@Composable fun CliplyButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) { Button(onClick, modifier.height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()) { Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(CliplyPrimary, CliplySecondary)), RoundedCornerShape(12.dp)), contentAlignment = androidx.compose.ui.Alignment.Center) { Text(text, color = Color.White) } } }
@Composable fun CliplyOutlinedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) { OutlinedButton(onClick, modifier.height(52.dp), shape = RoundedCornerShape(12.dp), border = ButtonDefaults.outlinedButtonBorder) { Text(text) } }
@Composable fun CliplyCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) { Card(modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CliplySurface), border = androidx.compose.foundation.BorderStroke(1.dp, CliplyBorder), content = content) }
@Composable fun CliplyTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) { OutlinedTextField(value, onValueChange, modifier, placeholder = { Text(placeholder) }, singleLine = true, shape = RoundedCornerShape(12.dp)) }
@Composable fun CliplyProgressBar(progress: Float, modifier: Modifier = Modifier) { LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = modifier.height(8.dp), color = CliplyPrimary, trackColor = CliplySurface2) }
@Composable fun CliplyBottomNavigation(selected: String, onSelect: (String) -> Unit) { NavigationBar(containerColor = CliplySurface) { listOf("Home", "Downloads", "Settings").forEach { item -> NavigationBarItem(selected == item, { onSelect(item) }, icon = { Text(item.take(1)) }, label = { Text(item) }) } } }
