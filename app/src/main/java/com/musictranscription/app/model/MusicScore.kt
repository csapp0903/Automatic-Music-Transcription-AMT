package com.musictranscription.app.model

/**
 * Represents a complete music score
 */
data class MusicScore(
    val notes: List<Note>,
    val tempo: Int = 120,  // BPM
    val timeSignature: Pair<Int, Int> = Pair(4, 4),  // e.g., 4/4 time
    val keySignature: String = "C"  // Key signature (e.g., "C", "Am", "G")
) {
    /**
     * Get the total duration of the score in seconds
     */
    fun getTotalDuration(): Double {
        if (notes.isEmpty()) return 0.0
        return notes.maxOf { it.startTime + it.duration }
    }

    /**
     * Get notes within a time range
     */
    fun getNotesInRange(startTime: Double, endTime: Double): List<Note> {
        return notes.filter { note ->
            note.startTime < endTime && (note.startTime + note.duration) > startTime
        }
    }

    /**
     * Sort notes by start time
     */
    fun sortedByTime(): MusicScore {
        return copy(notes = notes.sortedBy { it.startTime })
    }
}
