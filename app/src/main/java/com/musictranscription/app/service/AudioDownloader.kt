package com.musictranscription.app.service

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Service for downloading audio files from URLs
 */
class AudioDownloader(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Download audio file from URL
     * @param url The URL to download from
     * @param progressCallback Callback for download progress (0.0 to 1.0)
     * @return The downloaded file
     */
    suspend fun downloadAudio(
        url: String,
        progressCallback: ((Float) -> Unit)? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Download failed: ${response.code}")
                )
            }

            val body = response.body ?: return@withContext Result.failure(
                Exception("Empty response body")
            )

            // Create temp file
            val fileName = "audio_${System.currentTimeMillis()}.${getFileExtension(url)}"
            val outputFile = File(context.cacheDir, fileName)

            // Download with progress
            val contentLength = body.contentLength()
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(outputFile)

            var totalBytesRead = 0L
            val buffer = ByteArray(8192)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                if (contentLength > 0) {
                    val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                    progressCallback?.invoke(progress)
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getFileExtension(url: String): String {
        return when {
            url.contains(".mp3", ignoreCase = true) -> "mp3"
            url.contains(".wav", ignoreCase = true) -> "wav"
            url.contains(".m4a", ignoreCase = true) -> "m4a"
            url.contains(".flac", ignoreCase = true) -> "flac"
            url.contains(".ogg", ignoreCase = true) -> "ogg"
            else -> "audio"
        }
    }
}
