package com.musictranscription.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musictranscription.app.viewmodel.MainViewModel
import com.musictranscription.app.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val audioUrl by viewModel.audioUrl.collectAsState()

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectAudioFile(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("音乐转谱") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "将音频转换为曲谱",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Audio input section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "选择音频源",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // File picker button
                    Button(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is UiState.Processing
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("选择音频文件")
                    }

                    HorizontalDivider()

                    // URL input
                    OutlinedTextField(
                        value = audioUrl,
                        onValueChange = { viewModel.updateAudioUrl(it) },
                        label = { Text("音频链接") },
                        placeholder = { Text("https://example.com/audio.mp3") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is UiState.Processing,
                        singleLine = true
                    )

                    Button(
                        onClick = { viewModel.downloadAudio(context) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is UiState.Processing && audioUrl.isNotBlank()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("下载音频")
                    }
                }
            }

            // Transcribe section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "转换曲谱",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Button(
                        onClick = { viewModel.transcribeAudio(context) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState is UiState.AudioLoaded || uiState is UiState.Transcribed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("开始转换")
                    }
                }
            }

            // Export section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "导出格式",
                        style = MaterialTheme.typography.titleMedium
                    )

                    val exportEnabled = uiState is UiState.Transcribed || uiState is UiState.Exported

                    OutlinedButton(
                        onClick = { viewModel.exportMidi(context) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = exportEnabled
                    ) {
                        Icon(Icons.Default.AudioFile, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("导出 MIDI")
                    }

                    OutlinedButton(
                        onClick = { viewModel.exportMusicXml(context) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = exportEnabled
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("导出 MusicXML")
                    }

                    OutlinedButton(
                        onClick = { viewModel.exportPdf(context) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = exportEnabled
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("导出 PDF")
                    }
                }
            }

            // Status section
            when (val state = uiState) {
                is UiState.Processing -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(state.message)
                            if (progress > 0) {
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("${(progress * 100).toInt()}%")
                            }
                        }
                    }
                }
                is UiState.AudioLoaded -> {
                    StatusCard(
                        message = "音频已加载: ${state.file.name}",
                        icon = Icons.Default.CheckCircle,
                        isSuccess = true
                    )
                }
                is UiState.Transcribed -> {
                    StatusCard(
                        message = "转换完成！检测到 ${state.score.notes.size} 个音符",
                        icon = Icons.Default.CheckCircle,
                        isSuccess = true
                    )
                }
                is UiState.Exported -> {
                    StatusCard(
                        message = "${state.format} 文件已导出:\n${state.file.absolutePath}",
                        icon = Icons.Default.CheckCircle,
                        isSuccess = true
                    )
                }
                is UiState.Error -> {
                    StatusCard(
                        message = state.message,
                        icon = Icons.Default.Error,
                        isSuccess = false
                    )
                }
                else -> {}
            }

            // Reset button
            if (uiState !is UiState.Idle && uiState !is UiState.Processing) {
                TextButton(
                    onClick = { viewModel.resetState() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("重置")
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatusCard(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSuccess: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSuccess)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSuccess)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
