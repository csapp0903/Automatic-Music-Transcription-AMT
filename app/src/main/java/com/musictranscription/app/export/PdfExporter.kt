package com.musictranscription.app.export

import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.musictranscription.app.model.MusicScore
import com.musictranscription.app.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 导出五线谱 PDF (图形绘制版) - 修复字体报错版
 */
class PdfExporter {

    // 配置常量
    private val MARGIN_LEFT = 50f
    private val MARGIN_RIGHT = 50f
    private val START_Y = 750f // A4 高度约 842

    private val STAFF_LINE_GAP = 6f // 五线谱线间距
    private val NOTE_RADIUS = 3.5f  // 音符半径
    private val NOTE_SPACING = 25f  // 音符水平间距
    private val SYSTEM_GAP = 80f    // 每一行谱表的间距

    /**
     * 导出 PDF
     */
    suspend fun export(score: MusicScore, outputFile: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val pdfWriter = PdfWriter(outputFile)
            val pdfDoc = PdfDocument(pdfWriter)
            val page = pdfDoc.addNewPage(PageSize.A4)
            val canvas = PdfCanvas(page)

            // 1. 绘制标题
            drawTitle(canvas, "Generated Sheet Music")

            // 2. 绘制五线谱和音符
            drawSheetMusic(canvas, score, pdfDoc)

            pdfDoc.close()
            Result.success(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun drawTitle(canvas: PdfCanvas, title: String) {
        // 修复 1: 使用 PdfFontFactory 创建字体对象
        val font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)

        canvas.beginText()
        canvas.setFontAndSize(font, 24f)
        canvas.moveText(200.0, 800.0) // 简单的居中位置估算
        canvas.showText(title)
        canvas.endText()
    }

    private fun drawSheetMusic(canvas: PdfCanvas, score: MusicScore, pdfDoc: PdfDocument) {
        var currentX = MARGIN_LEFT
        var currentY = START_Y
        // var currentPage = pdfDoc.getFirstPage() // 未使用变量
        var currentCanvas = canvas

        // 过滤并排序音符
        val sortedNotes = score.notes.sortedBy { it.startTime }

        // 绘制第一行谱表
        drawStaffSystem(currentCanvas, MARGIN_LEFT, currentY, PageSize.A4.width - MARGIN_RIGHT)
        drawTrebleClefSymbol(currentCanvas, MARGIN_LEFT, currentY)
        currentX += 40f // 给谱号留空间

        for (note in sortedNotes) {
            // 检查是否需要换行
            if (currentX > PageSize.A4.width - MARGIN_RIGHT) {
                currentX = MARGIN_LEFT + 20f // 重置 X
                currentY -= SYSTEM_GAP // 下移 Y

                // 检查是否需要换页
                if (currentY < 50f) {
                    val newPage = pdfDoc.addNewPage(PageSize.A4)
                    currentCanvas = PdfCanvas(newPage)
                    currentY = START_Y
                }

                // 在新位置绘制五线谱
                drawStaffSystem(currentCanvas, MARGIN_LEFT, currentY, PageSize.A4.width - MARGIN_RIGHT)
                drawTrebleClefSymbol(currentCanvas, MARGIN_LEFT, currentY)
                currentX += 40f
            }

            // 绘制音符
            drawNote(currentCanvas, note, currentX, currentY)
            currentX += NOTE_SPACING
        }
    }

    /**
     * 绘制五条线
     */
    private fun drawStaffSystem(canvas: PdfCanvas, x: Float, y: Float, endX: Float) {
        canvas.setStrokeColor(ColorConstants.BLACK)
        canvas.setLineWidth(0.5f)

        for (i in 0..4) {
            val lineY = y - (i * STAFF_LINE_GAP)
            canvas.moveTo(x.toDouble(), lineY.toDouble())
            canvas.lineTo(endX.toDouble(), lineY.toDouble())
            canvas.stroke()
        }

        // 绘制结尾竖线
        canvas.moveTo(endX.toDouble(), y.toDouble())
        canvas.lineTo(endX.toDouble(), (y - 4 * STAFF_LINE_GAP).toDouble())
        canvas.stroke()

        canvas.moveTo(x.toDouble(), y.toDouble())
        canvas.lineTo(x.toDouble(), (y - 4 * STAFF_LINE_GAP).toDouble())
        canvas.stroke()
    }

    /**
     * 简单的文本替代高音谱号
     */
    private fun drawTrebleClefSymbol(canvas: PdfCanvas, x: Float, y: Float) {
        // 修复 2: 使用正确的常量名 TIMES_BOLDITALIC 并创建字体对象
        val font = PdfFontFactory.createFont(StandardFonts.TIMES_BOLDITALIC)

        canvas.beginText()
        canvas.setFontAndSize(font, 30f)
        // 调整位置使其看起来像谱号
        canvas.moveText(x.toDouble(), (y - 3 * STAFF_LINE_GAP - 5).toDouble())
        canvas.showText("G") // 使用花体 G 代表高音谱号
        canvas.endText()
    }

    /**
     * 绘制单个音符
     */
    private fun drawNote(canvas: PdfCanvas, note: Note, x: Float, staffTopY: Float) {
        val stepsFromF5 = getStepsFromF5(note.pitch)
        val noteY = staffTopY - (stepsFromF5 * (STAFF_LINE_GAP / 2f))

        // 绘制符头 (实心圆)
        canvas.setFillColor(ColorConstants.BLACK)
        canvas.circle(x.toDouble(), noteY.toDouble(), NOTE_RADIUS.toDouble())
        canvas.fill()

        // 绘制符干 (向上或向下)
        val isStemUp = note.pitch < 71 // B4
        val stemHeight = 25f

        canvas.setLineWidth(1f)
        // 重置描边颜色，防止之前的填充颜色影响描边
        canvas.setStrokeColor(ColorConstants.BLACK)

        if (isStemUp) {
            // 符干在右侧，向上
            canvas.moveTo((x + NOTE_RADIUS).toDouble(), noteY.toDouble())
            canvas.lineTo((x + NOTE_RADIUS).toDouble(), (noteY + stemHeight).toDouble())
        } else {
            // 符干在左侧，向下
            canvas.moveTo((x - NOTE_RADIUS).toDouble(), noteY.toDouble())
            canvas.lineTo((x - NOTE_RADIUS).toDouble(), (noteY - stemHeight).toDouble())
        }
        canvas.stroke()

        // 绘制加线 (Ledger Lines)
        val topY = staffTopY
        val bottomY = staffTopY - 4 * STAFF_LINE_GAP

        // 检查是否在上方
        if (noteY > topY + STAFF_LINE_GAP/2) {
            var lineY = topY + STAFF_LINE_GAP
            while (lineY <= noteY) {
                drawLedgerLine(canvas, x, lineY)
                lineY += STAFF_LINE_GAP
            }
        }
        // 检查是否在下方 (如 C4)
        else if (noteY < bottomY - STAFF_LINE_GAP/2) {
            var lineY = bottomY - STAFF_LINE_GAP
            while (lineY >= noteY) {
                drawLedgerLine(canvas, x, lineY)
                lineY -= STAFF_LINE_GAP
            }
        }

        // 简单的升降号处理 (可选)
        if (isSharp(note.pitch)) {
            // 修复 3: 此处同样需要创建字体对象
            val font = PdfFontFactory.createFont(StandardFonts.HELVETICA)

            canvas.beginText()
            canvas.setFontAndSize(font, 10f)
            canvas.moveText((x - 10).toDouble(), (noteY - 3).toDouble())
            canvas.showText("#")
            canvas.endText()
        }
    }

    private fun drawLedgerLine(canvas: PdfCanvas, x: Float, y: Float) {
        val lineLen = 14f
        canvas.moveTo((x - lineLen/2).toDouble(), y.toDouble())
        canvas.lineTo((x + lineLen/2).toDouble(), y.toDouble())
        canvas.stroke()
    }

    /**
     * 计算自然音阶下距离 F5 的步数
     */
    private fun getStepsFromF5(midiPitch: Int): Int {
        val offsetMap = intArrayOf(0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6) // C to B 的自然音阶索引

        val octave = (midiPitch / 12)
        val noteIndex = midiPitch % 12

        val diatonicIndex = octave * 7 + offsetMap[noteIndex]

        // F5 的 Diatonic Index (F5 is octave 5, offset 3)
        val f5DiatonicIndex = 5 * 7 + 3

        return f5DiatonicIndex - diatonicIndex
    }

    private fun isSharp(midiPitch: Int): Boolean {
        val noteIndex = midiPitch % 12
        // C C# D D# E F F# G G# A A# B
        // 0 1  2 3  4 5 6  7 8  9 10 11
        return noteIndex == 1 || noteIndex == 3 || noteIndex == 6 || noteIndex == 8 || noteIndex == 10
    }
}