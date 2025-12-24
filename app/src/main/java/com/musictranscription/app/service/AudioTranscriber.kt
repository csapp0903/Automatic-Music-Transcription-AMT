package com.musictranscription.app.service

import android.content.Context
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.TarsosDSPAudioInputStream
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor
import com.musictranscription.app.model.MusicScore
import com.musictranscription.app.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.ln
import kotlin.math.roundToInt

class AudioTranscriber(private val context: Context) {

    companion object {
        private const val TAG = "AudioTranscriber"
    }

    suspend fun transcribe(
        audioFile: File,
        progressCallback: ((Float) -> Unit)? = null
    ): Result<MusicScore> = withContext(Dispatchers.IO) {
        try {
            progressCallback?.invoke(0.05f)
            Log.d(TAG, "开始处理文件: ${audioFile.name}")

            // 1. 格式转换 (MP3 -> WAV)
            // 这一步确保文件是标准的 PCM 16bit, 44100Hz, 单声道
            val wavFile = convertToWav(audioFile)
            progressCallback?.invoke(0.2f)

            // 2. 音频分析 (使用自定义流读取，防止 TarsosDSP 崩溃)
            val notes = analyzeAudioWithTarsos(wavFile, progressCallback)
            Log.d(TAG, "分析完成，检测到 ${notes.size} 个音符")

            // 3. 生成曲谱对象
            val score = MusicScore(
                notes = notes,
                tempo = 120,
                timeSignature = Pair(4, 4),
                keySignature = "C"
            )

            progressCallback?.invoke(1.0f)
            Result.success(score)
        } catch (e: Exception) {
            Log.e(TAG, "转谱失败", e)
            Result.failure(e)
        }
    }

    private suspend fun convertToWav(inputFile: File): File = withContext(Dispatchers.IO) {
        val outputFile = File(context.cacheDir, "converted_${System.currentTimeMillis()}.wav")
        // 强制转换为 PCM 16bit (s16le), 44100Hz, 单声道 (ac 1)
        // 这些参数必须与下面的 TarsosDSPAudioFormat 匹配
        val cmd = "-y -i \"${inputFile.absolutePath}\" -vn -acodec pcm_s16le -ar 44100 -ac 1 \"${outputFile.absolutePath}\""

        Log.d(TAG, "执行 FFmpeg: $cmd")
        val rc = FFmpeg.execute(cmd)

        if (rc == Config.RETURN_CODE_SUCCESS) {
            return@withContext outputFile
        } else {
            throw Exception("FFmpeg 转换失败，错误码: $rc")
        }
    }

    private fun analyzeAudioWithTarsos(
        wavFile: File,
        progressCallback: ((Float) -> Unit)?
    ): List<Note> {
        val detectedNotes = mutableListOf<Note>()

        val sampleRate = 44100f
        val bufferSize = 2048
        val overlap = 0

        // --- 核心修复开始 ---
        // 不使用 AudioDispatcherFactory.fromPipe，改用自定义文件流
        // 我们确信文件是 PCM 16bit, 单声道, Little Endian (由 FFmpeg 保证)
        val format = TarsosDSPAudioFormat(sampleRate, 16, 1, true, false)
        val audioStream = AndroidWavInputStream(wavFile, format)
        val dispatcher = AudioDispatcher(audioStream, bufferSize, overlap)
        // --- 核心修复结束 ---

        var currentNoteStart = -1.0
        var currentMidiPitch = -1

        val pitchHandler = PitchDetectionHandler { result: PitchDetectionResult, audioEvent: AudioEvent ->
            val pitchInHz = result.pitch
            val timeStamp = audioEvent.timeStamp

            // 更新进度
            if (timeStamp % 0.5 < 0.05) {
                val progress = 0.2f + (timeStamp.toFloat() / 180.0f).coerceAtMost(0.7f)
                progressCallback?.invoke(progress)
            }

            if (pitchInHz != -1f) {
                val midiPitch = frequencyToMidi(pitchInHz.toDouble())

                if (midiPitch != currentMidiPitch) {
                    if (currentMidiPitch != -1 && (timeStamp - currentNoteStart) > 0.1) {
                        detectedNotes.add(Note(currentMidiPitch, currentNoteStart, timeStamp - currentNoteStart, 90))
                    }
                    currentMidiPitch = midiPitch
                    currentNoteStart = timeStamp
                }
            } else {
                if (currentMidiPitch != -1 && (timeStamp - currentNoteStart) > 0.1) {
                    detectedNotes.add(Note(currentMidiPitch, currentNoteStart, timeStamp - currentNoteStart, 90))
                    currentMidiPitch = -1
                }
            }
        }

        dispatcher.addAudioProcessor(PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.YIN,
            sampleRate,
            bufferSize,
            pitchHandler
        ))

        dispatcher.run()

        return detectedNotes
    }

    private fun frequencyToMidi(frequency: Double): Int {
        if (frequency <= 0) return 0
        return (69 + 12 * (ln(frequency / 440.0) / ln(2.0))).roundToInt()
    }

    /**
     * 自定义 WAV 输入流，用于 Android 平台
     * 简单地跳过 WAV 头部 (44字节)，直接读取 PCM 数据
     */
    private class AndroidWavInputStream(file: File, private val format: TarsosDSPAudioFormat) : TarsosDSPAudioInputStream {
        private val fileInputStream = FileInputStream(file)

        init {
            // 跳过标准的 WAV 头部 (44 bytes)
            // 既然是我们自己用 FFmpeg 生成的标准 WAV，可以直接跳过
            fileInputStream.skip(44)
        }

        override fun skip(bytesToSkip: Long): Long {
            return fileInputStream.skip(bytesToSkip)
        }

        override fun read(b: ByteArray?, off: Int, len: Int): Int {
            return fileInputStream.read(b, off, len)
        }

        override fun close() {
            fileInputStream.close()
        }

        override fun getFormat(): TarsosDSPAudioFormat {
            return format
        }

        override fun getFrameLength(): Long {
            return -1 // 未知长度，不影响分析
        }
    }
}