package com.zaheer.imagetopdf.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zaheer.imagetopdf.MainActivity
import com.zaheer.imagetopdf.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isProUser by viewModel.isProUser.collectAsState()
    val message by viewModel.message.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pro Features") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (isProUser) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Pro Active",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Pro Active ✓",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Thank you for upgrading to Pro!",
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Text(
                    "Upgrade to Pro",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Unlock all premium features with a one-time purchase",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Pro Features:",
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            FeatureItem(
                title = "Unlimited Images",
                description = "Add unlimited images per PDF without restrictions",
                enabled = isProUser
            )
            
            FeatureItem(
                title = "No Watermark",
                description = "Generate clean PDFs without watermarks",
                enabled = isProUser
            )
            
            FeatureItem(
                title = "High Resolution Export",
                description = "Export PDFs with the highest quality settings",
                enabled = isProUser
            )
            
            FeatureItem(
                title = "Batch PDF Creation",
                description = "Create multiple PDFs from image groups efficiently",
                enabled = isProUser
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (!isProUser) {
                Button(
                    onClick = {
                        if (context is MainActivity) {
                            viewModel.purchasePro(context)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Unlock Pro - One-time Purchase")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { viewModel.restorePurchases() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Restore Purchases")
                }
            } else {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Home")
                }
            }
        }
    }
}

@Composable
fun FeatureItem(
    title: String,
    description: String,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = if (enabled) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "• $title",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (enabled) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Enabled",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
