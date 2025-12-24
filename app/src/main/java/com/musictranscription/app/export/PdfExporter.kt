package com.musictranscription.app.export

import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.musictranscription.app.model.MusicScore
import com.musictranscription.app.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Export music score to PDF format
 * This creates a simplified sheet music representation
 */
class PdfExporter {

    /**
     * Export music score to PDF file
     * @param score The music score to export
     * @param outputFile The output PDF file
     * @return Result with the output file path
     */
    suspend fun export(score: MusicScore, outputFile: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val pdfWriter = PdfWriter(outputFile)
            val pdfDoc = PdfDocument(pdfWriter)
            val document = Document(pdfDoc, PageSize.A4)

            // Add title
            val title = Paragraph("音乐曲谱")
                .setFontSize(24f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
            document.add(title)

            // Add metadata
            val metadata = Paragraph(
                "速度: ${score.tempo} BPM | " +
                "拍号: ${score.timeSignature.first}/${score.timeSignature.second} | " +
                "调号: ${score.keySignature}"
            )
                .setFontSize(12f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f)
            document.add(metadata)

            // Add note table
            val table = Table(UnitValue.createPercentArray(floatArrayOf(15f, 25f, 20f, 20f, 20f)))
                .useAllAvailableWidth()

            // Table header
            table.addHeaderCell(createHeaderCell("序号"))
            table.addHeaderCell(createHeaderCell("音符"))
            table.addHeaderCell(createHeaderCell("开始时间(秒)"))
            table.addHeaderCell(createHeaderCell("持续时间(秒)"))
            table.addHeaderCell(createHeaderCell("力度"))

            // Add notes
            score.notes.sortedBy { it.startTime }.forEachIndexed { index, note ->
                table.addCell((index + 1).toString())
                table.addCell(note.getNoteName())
                table.addCell(String.format("%.2f", note.startTime))
                table.addCell(String.format("%.2f", note.duration))
                table.addCell(note.velocity.toString())
            }

            document.add(table)

            // Add summary
            val summary = Paragraph("\n总音符数: ${score.notes.size}")
                .setFontSize(12f)
                .setMarginTop(20f)
            document.add(summary)

            val totalDuration = Paragraph("总时长: ${String.format("%.2f", score.getTotalDuration())} 秒")
                .setFontSize(12f)
            document.add(totalDuration)

            // Add staff notation representation (simplified)
            document.add(Paragraph("\n五线谱表示 (简化):").setFontSize(14f).setBold().setMarginTop(20f))
            document.add(createSimplifiedStaffNotation(score))

            document.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createHeaderCell(text: String): com.itextpdf.layout.element.Cell {
        return com.itextpdf.layout.element.Cell()
            .add(Paragraph(text).setBold())
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setTextAlignment(TextAlignment.CENTER)
    }

    private fun createSimplifiedStaffNotation(score: MusicScore): Table {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(20f, 80f)))
            .useAllAvailableWidth()

        // Group notes by time
        val timeGroupedNotes = score.notes.sortedBy { it.startTime }
            .groupBy { (it.startTime * 2).toInt() / 2.0 } // Group by 0.5 second intervals

        timeGroupedNotes.forEach { (time, notes) ->
            table.addCell(String.format("%.1fs", time))
            val noteNames = notes.joinToString(", ") { it.getNoteName() }
            table.addCell(noteNames)
        }

        return table
    }
}
