package com.musictranscription.app.export

import android.content.Context
import com.google.gson.Gson
import com.musictranscription.app.model.MusicScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class HtmlScoreExporter(private val context: Context) {

    suspend fun export(score: MusicScore, outputFile: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            // 1. 读取 Assets 中的模板
            val template = context.assets.open("sheet_music_template.html")
                .bufferedReader()
                .use { it.readText() }

            // 2. 将 Score 数据转为 JSON
            val gson = Gson()
            val scoreJson = gson.toJson(score)

            // 3. 将 JSON 注入到 HTML 的 JS 调用中
            // 我们在 HTML 底部追加一段脚本来调用 renderScore
            val injectionScript = """
                <script>
                    const data = $scoreJson;
                    renderScore(data);
                </script>
            """

            val finalHtml = template.replace("</body>", "$injectionScript</body>")

            // 4. 写入文件
            outputFile.writeText(finalHtml)

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}