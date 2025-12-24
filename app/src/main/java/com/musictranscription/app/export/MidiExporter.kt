package com.musictranscription.app.export

import com.leff.midi.MidiFile
import com.leff.midi.MidiTrack
import com.leff.midi.event.NoteOff
import com.leff.midi.event.NoteOn
import com.leff.midi.event.meta.Tempo
import com.leff.midi.event.meta.TimeSignature
import com.musictranscription.app.model.MusicScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Export music score to MIDI format
 */
class MidiExporter {

    /**
     * Export music score to MIDI file
     * @param score The music score to export
     * @param outputFile The output MIDI file
     * @return Result with the output file path
     */
    suspend fun export(score: MusicScore, outputFile: File): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Create MIDI file with one track
            val tempoTrack = MidiTrack()
            val noteTrack = MidiTrack()

            // Set tempo (microseconds per quarter note)
            val microsecondsPerQuarterNote = (60_000_000.0 / score.tempo).toLong()
            val tempo = Tempo()
            tempo.mpqn = microsecondsPerQuarterNote.toInt()
            tempoTrack.insertEvent(tempo)

            // Set time signature
//            val (numerator, denominator) = score.timeSignature
//            val timeSignature = TimeSignature()
//            timeSignature.numerator = numerator
//            timeSignature.realDenominator = denominator
//            tempoTrack.insertEvent(timeSignature)
            // Set time signature
            val (numerator, denominator) = score.timeSignature
            val timeSignature = TimeSignature()
            // 使用专门的方法设置拍号，后两个参数使用默认值即可
            timeSignature.setTimeSignature(numerator, denominator, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION)
            tempoTrack.insertEvent(timeSignature)

            // Resolution (ticks per quarter note)
            val resolution = 480

            // Add notes to track
            for (note in score.notes.sortedBy { it.startTime }) {
                // Calculate tick positions
                val ticksPerSecond = (resolution * score.tempo) / 60.0
                val startTick = (note.startTime * ticksPerSecond).toLong()
                val durationTicks = (note.duration * ticksPerSecond).toLong()

                // Note on event
                val noteOn = NoteOn(startTick, 0, note.pitch, note.velocity)
                noteTrack.insertEvent(noteOn)

                // Note off event
                val noteOff = NoteOff(startTick + durationTicks, 0, note.pitch, 0)
                noteTrack.insertEvent(noteOff)
            }

            // Create MIDI file
            val tracks = listOf(tempoTrack, noteTrack)
            val midiFile = MidiFile(resolution, tracks)

            // Write to file
            midiFile.writeToFile(outputFile)

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
