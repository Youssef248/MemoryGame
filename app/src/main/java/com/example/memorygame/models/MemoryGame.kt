package com.example.memorygame.models

import com.example.memorygame.utils.DEFAULT_ICONS


// Import necessary classes and modules
class MemoryGame(private val boardSize: BoardSize, private val customImages: List<String>?) {

    // Declare properties
    val cards: List<MemoryCard>
    var numPairsFound = 0
    private var numCardFlips = 0
    private var indexOfSingleSelectedCard: Int? = null

    // Initialize properties in the constructor
    init {
        if (customImages == null) {
            // Use default images if custom images are not provided
            val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
            val randomizedImages = (chosenImages + chosenImages).shuffled()
            cards = randomizedImages.map { MemoryCard(it) }
        } else {
            // Use custom images if provided
            val randomizedImages = (customImages + customImages).shuffled()
            cards = randomizedImages.map { MemoryCard(it.hashCode(), it) }
        }
    }

    // Function to handle card flips
    fun flipCard(position: Int): Boolean {
        numCardFlips++
        val card = cards[position]

        // Three cases for handling card flips
        when {
            indexOfSingleSelectedCard == null -> {
                // No cards are flipped yet, or two cards are already flipped
                restoreCards()
                indexOfSingleSelectedCard = position
            }
            areCardsTheSame(indexOfSingleSelectedCard!!, position) -> {
                // Two cards match
                cards[indexOfSingleSelectedCard!!].isMatched = true
                cards[position].isMatched = true
                numPairsFound++
                indexOfSingleSelectedCard = null
            }
            else -> {
                // Two cards are flipped, but they don't match
                indexOfSingleSelectedCard = null
            }
        }

        // Flip the selected card
        card.isFaceUp = !card.isFaceUp
        return indexOfSingleSelectedCard != null
    }

    // Function to restore the state of face-up cards
    private fun restoreCards() {
        for (card in cards) {
            if (!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }

    // Function to check if two cards have the same identifier
    private fun areCardsTheSame(pos1: Int, pos2: Int): Boolean {
        return cards[pos1].identifier == cards[pos2].identifier
    }

    // Function to check if the player has won the game
    fun haveWonGame(): Boolean {
        return numPairsFound == boardSize.getNumPairs()
    }

    // Function to check if a card at a given position is face up
    fun isCardFaceUp(position: Int): Boolean {
        return cards[position].isFaceUp
    }

    // Function to get the number of moves made in the game
    fun getNumMoves(): Int {
        // One move is counted for every two card flips
        return numCardFlips / 2
    }
}