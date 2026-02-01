package com.zaheer.imagetopdf.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zaheer.imagetopdf.model.PageSize
import com.zaheer.imagetopdf.model.PdfSettings
import com.zaheer.imagetopdf.model.Quality
import com.zaheer.imagetopdf.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigateToPreview: () -> Unit,
    onNavigateToPro: () -> Unit
) {
    val images by viewModel.images.collectAsState()
    val pdfSettings by viewModel.pdfSettings.collectAsState()
    val isProUser by viewModel.isProUser.collectAsState()
    val message by viewModel.message.collectAsState()
    
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }
    
    // Show message snackbar
    LaunchedEffect(message) {
        message?.let {
            // Message will be shown by SnackbarHost
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image to PDF Converter") },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                    if (isProUser) {
                        Text(
                            "Pro ✓",
                            modifier = Modifier.padding(end = 16.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        TextButton(onClick = onNavigateToPro) {
                            Text("Upgrade to Pro")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                Icon(Icons.Default.Add, "Add Images")
            }
        },
        snackbarHost = {
            message?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearMessage() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(msg)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (images.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No images selected\nTap + to add images",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${images.size} image(s) selected",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    if (!isProUser && images.size >= 10) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                "Free version limited to 10 images. Upgrade to Pro for unlimited!",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(images) { image ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        image.uri.lastPathSegment ?: "Image",
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (image.rotationDegrees > 0) {
                                        Text(
                                            "↻ ${image.rotationDegrees.toInt()}°",
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Button(
                    onClick = onNavigateToPreview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Preview & Generate PDF")
                }
            }
        }
        
        if (showSettingsDialog) {
            SettingsDialog(
                currentSettings = pdfSettings,
                isProUser = isProUser,
                onDismiss = { showSettingsDialog = false },
                onConfirm = { newSettings ->
                    viewModel.updatePdfSettings(newSettings)
                    showSettingsDialog = false
                }
            )
        }
    }
}

@Composable
fun SettingsDialog(
    currentSettings: PdfSettings,
    isProUser: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (PdfSettings) -> Unit
) {
    var pageSize by remember { mutableStateOf(currentSettings.pageSize) }
    var margin by remember { mutableStateOf(currentSettings.margin) }
    var quality by remember { mutableStateOf(currentSettings.quality) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PDF Settings") },
        text = {
            Column {
                Text("Page Size", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PageSize.values().forEach { size ->
                        FilterChip(
                            selected = pageSize == size,
                            onClick = { pageSize = size },
                            label = { Text(size.name) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Margin: ${margin}px", style = MaterialTheme.typography.titleSmall)
                Slider(
                    value = margin.toFloat(),
                    onValueChange = { margin = it.toInt() },
                    valueRange = 0f..50f
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Quality", style = MaterialTheme.typography.titleSmall)
                Column {
                    Quality.values().forEach { q ->
                        val enabled = q != Quality.HIGH || isProUser
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = quality == q,
                                onClick = { if (enabled) quality = q },
                                enabled = enabled
                            )
                            Text(
                                text = q.name + if (q == Quality.HIGH && !isProUser) " (Pro)" else "",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(PdfSettings(pageSize, margin, quality))
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
