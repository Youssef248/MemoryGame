package com.example.memorygame.models

// Enum class representing different board sizes
enum class BoardSize(val numCards: Int) {
    EASY(8),
    MEDIUM(18),
    HARD(24);

    companion object {
        // Function to get BoardSize by a specific value (number of cards)
        fun getByValue(value: Int) = values().first { it.numCards == value }
    }

    // Function to get the width of the board based on the difficulty level
    fun getWidth(): Int {
        return when (this) {
            EASY -> 2
            MEDIUM -> 3
            HARD -> 4
        }
    }

    // Function to get the height of the board based on the difficulty level
    fun getHeight(): Int {
        return numCards / getWidth()
    }

    // Function to get the number of pairs on the board based on the difficulty level
    fun getNumPairs(): Int {
        return numCards / 2
    }
}