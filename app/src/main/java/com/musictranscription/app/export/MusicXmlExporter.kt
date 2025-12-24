package com.musictranscription.app.export

import com.musictranscription.app.model.MusicScore
import com.musictranscription.app.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

/**
 * Export music score to MusicXML format
 */
class MusicXmlExporter {

    /**
     * Export music score to MusicXML file
     * @param score The music score to export
     * @param outputFile The output MusicXML file
     * @return Result with the output file path
     */
    suspend fun export(score: MusicScore, outputFile: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            val xml = buildMusicXml(score)
            outputFile.writeText(xml)
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildMusicXml(score: MusicScore): String {
        val (numerator, denominator) = score.timeSignature

        return """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE score-partwise PUBLIC "-//Recordare//DTD MusicXML 3.1 Partwise//EN" "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="3.1">
  <work>
    <work-title>Transcribed Music</work-title>
  </work>
  <identification>
    <creator type="software">Music Transcription App</creator>
    <encoding>
      <software>Music Transcription App</software>
      <encoding-date>${getCurrentDate()}</encoding-date>
    </encoding>
  </identification>
  <part-list>
    <score-part id="P1">
      <part-name>Piano</part-name>
    </score-part>
  </part-list>
  <part id="P1">
${buildMeasures(score)}
  </part>
</score-partwise>"""
    }

    private fun buildMeasures(score: MusicScore): String {
        val measures = StringBuilder()
        val (numerator, denominator) = score.timeSignature
        val beatsPerMeasure = numerator
        val beatDuration = 4.0 / denominator // Duration of one beat in quarter notes
        val measureDuration = beatsPerMeasure * beatDuration

        // Group notes by measures
        val notesByMeasure = groupNotesByMeasure(score, measureDuration)

        notesByMeasure.forEachIndexed { index, notesInMeasure ->
            measures.append(buildMeasure(index + 1, notesInMeasure, score, index == 0))
        }

        return measures.toString()
    }

    private fun groupNotesByMeasure(score: MusicScore, measureDuration: Double): List<List<Note>> {
        val result = mutableListOf<List<Note>>()
        val totalDuration = score.getTotalDuration()
        val measureCount = (totalDuration / measureDuration).roundToInt().coerceAtLeast(1)

        for (i in 0 until measureCount) {
            val measureStart = i * measureDuration
            val measureEnd = (i + 1) * measureDuration
            val notesInMeasure = score.notes.filter { note ->
                note.startTime >= measureStart && note.startTime < measureEnd
            }
            result.add(notesInMeasure)
        }

        return result
    }

    private fun buildMeasure(
        measureNumber: Int,
        notes: List<Note>,
        score: MusicScore,
        isFirst: Boolean
    ): String {
        val (numerator, denominator) = score.timeSignature
        val attributes = if (isFirst) """
    <measure number="$measureNumber">
      <attributes>
        <divisions>4</divisions>
        <key>
          <fifths>0</fifths>
        </key>
        <time>
          <beats>$numerator</beats>
          <beat-type>$denominator</beat-type>
        </time>
        <clef>
          <sign>G</sign>
          <line>2</line>
        </clef>
      </attributes>
      <direction placement="above">
        <direction-type>
          <metronome>
            <beat-unit>quarter</beat-unit>
            <per-minute>${score.tempo}</per-minute>
          </metronome>
        </direction-type>
      </direction>
${buildNotes(notes)}
    </measure>
""" else """
    <measure number="$measureNumber">
${buildNotes(notes)}
    </measure>
"""
        return attributes
    }

    private fun buildNotes(notes: List<Note>): String {
        if (notes.isEmpty()) {
            return """      <note>
        <rest/>
        <duration>16</duration>
        <voice>1</voice>
        <type>whole</type>
      </note>"""
        }

        val result = StringBuilder()
        for (note in notes) {
            result.append(buildNote(note))
        }
        return result.toString()
    }

    private fun buildNote(note: Note): String {
        val (step, octave, alter) = midiToPitch(note.pitch)
        val duration = (note.duration * 4).roundToInt().coerceAtLeast(1) // Convert to divisions
        val noteType = getDurationType(duration)

        val alterXml = if (alter != 0) "        <alter>$alter</alter>\n" else ""

        return """      <note>
        <pitch>
          <step>$step</step>
$alterXml          <octave>$octave</octave>
        </pitch>
        <duration>$duration</duration>
        <voice>1</voice>
        <type>$noteType</type>
      </note>
"""
    }

    private fun midiToPitch(midiNote: Int): Triple<String, Int, Int> {
        val noteNames = arrayOf("C", "C", "D", "D", "E", "F", "F", "G", "G", "A", "A", "B")
        val alterations = arrayOf(0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0) // Sharps
        val octave = (midiNote / 12) - 1
        val noteIndex = midiNote % 12
        return Triple(noteNames[noteIndex], octave, alterations[noteIndex])
    }

    private fun getDurationType(duration: Int): String {
        return when {
            duration >= 16 -> "whole"
            duration >= 8 -> "half"
            duration >= 4 -> "quarter"
            duration >= 2 -> "eighth"
            else -> "16th"
        }
    }

    private fun getCurrentDate(): String {
        return java.time.LocalDate.now().toString()
    }
}
