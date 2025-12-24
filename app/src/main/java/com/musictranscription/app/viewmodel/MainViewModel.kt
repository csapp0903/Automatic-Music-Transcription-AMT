package com.musictranscription.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musictranscription.app.export.MidiExporter
import com.musictranscription.app.export.MusicXmlExporter
import com.musictranscription.app.export.PdfExporter
import com.musictranscription.app.model.MusicScore
import com.musictranscription.app.service.AudioDownloader
import com.musictranscription.app.service.AudioTranscriber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Main ViewModel for the music transcription app
 */
class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _audioUrl = MutableStateFlow("")
    val audioUrl: StateFlow<String> = _audioUrl.asStateFlow()

    private var currentAudioFile: File? = null
    private var currentMusicScore: MusicScore? = null

    /**
     * Update audio URL
     */
    fun updateAudioUrl(url: String) {
        _audioUrl.value = url
    }

    /**
     * Select audio file from URI
     */
    fun selectAudioFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Processing("正在加载音频文件...")
                _progress.value = 0.1f

                // Copy file to cache
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("无法打开文件")

                val fileName = "audio_${System.currentTimeMillis()}.audio"
                val audioFile = File(context.cacheDir, fileName)

                inputStream.use { input ->
                    audioFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                currentAudioFile = audioFile
                _progress.value = 1.0f
                _uiState.value = UiState.AudioLoaded(audioFile)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("加载音频失败: ${e.message}")
            }
        }
    }

    /**
     * Download audio from URL
     */
    fun downloadAudio(context: Context) {
        val url = _audioUrl.value
        if (url.isBlank()) {
            _uiState.value = UiState.Error("请输入音频链接")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = UiState.Processing("正在下载音频...")
                _progress.value = 0f

                val downloader = AudioDownloader(context)
                val result = downloader.downloadAudio(url) { progress ->
                    _progress.value = progress
                }

                result.fold(
                    onSuccess = { file ->
                        currentAudioFile = file
                        _uiState.value = UiState.AudioLoaded(file)
                    },
                    onFailure = { error ->
                        _uiState.value = UiState.Error("下载失败: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("下载失败: ${e.message}")
            }
        }
    }

    /**
     * Transcribe audio to music score
     */
    fun transcribeAudio(context: Context) {
        val audioFile = currentAudioFile
        if (audioFile == null) {
            _uiState.value = UiState.Error("请先选择或下载音频文件")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = UiState.Processing("正在转换曲谱...")
                _progress.value = 0f

                val transcriber = AudioTranscriber(context)
                val result = transcriber.transcribe(audioFile) { progress ->
                    _progress.value = progress
                }

                result.fold(
                    onSuccess = { score ->
                        currentMusicScore = score
                        _uiState.value = UiState.Transcribed(score)
                    },
                    onFailure = { error ->
                        _uiState.value = UiState.Error("转换失败: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("转换失败: ${e.message}")
            }
        }
    }

    /**
     * Export to MIDI
     */
    fun exportMidi(context: Context) {
        exportFile(context, "midi") { score, file ->
            MidiExporter().export(score, file)
        }
    }

    /**
     * Export to MusicXML
     */
    fun exportMusicXml(context: Context) {
        exportFile(context, "musicxml") { score, file ->
            MusicXmlExporter().export(score, file)
        }
    }

    /**
     * Export to PDF
     */
    fun exportPdf(context: Context) {
        exportFile(context, "pdf") { score, file ->
            PdfExporter().export(score, file)
        }
    }

    private fun exportFile(
        context: Context,
        format: String,
        exporter: suspend (MusicScore, File) -> Result<File>
    ) {
        val score = currentMusicScore
        if (score == null) {
            _uiState.value = UiState.Error("请先转换曲谱")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = UiState.Processing("正在导出 ${format.uppercase()}...")
                _progress.value = 0.5f

                val extension = when (format) {
                    "midi" -> "mid"
                    "musicxml" -> "xml"
                    else -> format
                }

                val fileName = "music_${System.currentTimeMillis()}.$extension"
                val outputDir = File(context.getExternalFilesDir(null), "exports")
                outputDir.mkdirs()
                val outputFile = File(outputDir, fileName)

                val result = exporter(score, outputFile)

                result.fold(
                    onSuccess = { file ->
                        _progress.value = 1.0f
                        _uiState.value = UiState.Exported(file, format.uppercase())
                    },
                    onFailure = { error ->
                        _uiState.value = UiState.Error("导出失败: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("导出失败: ${e.message}")
            }
        }
    }

    /**
     * Reset to idle state
     */
    fun resetState() {
        _uiState.value = UiState.Idle
        _progress.value = 0f
    }
}

/**
 * UI State
 */
sealed class UiState {
    object Idle : UiState()
    data class Processing(val message: String) : UiState()
    data class AudioLoaded(val file: File) : UiState()
    data class Transcribed(val score: MusicScore) : UiState()
    data class Exported(val file: File, val format: String) : UiState()
    data class Error(val message: String) : UiState()
}
