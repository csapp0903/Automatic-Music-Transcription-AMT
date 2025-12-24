package com.musictranscription.app.model

/**
 * Represents a musical note
 * @param pitch MIDI pitch number (0-127)
 * @param startTime Start time in seconds
 * @param duration Duration in seconds
 * @param velocity Note velocity (0-127)
 */
data class Note(
    val pitch: Int,
    val startTime: Double,
    val duration: Double,
    val velocity: Int = 64
) {
    /**
     * Get the note name (e.g., "C4", "A#3")
     */
    fun getNoteName(): String {
        val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val octave = (pitch / 12) - 1
        val noteName = noteNames[pitch % 12]
        return "$noteName$octave"
    }

    /**
     * Get the frequency in Hz
     */
    fun getFrequency(): Double {
        return 440.0 * Math.pow(2.0, (pitch - 69) / 12.0)
    }
}
