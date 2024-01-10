package com.example.memorygame.models

import com.example.memorygame.utils.DEFAULT_ICONS

class MemoryGame(private val boardSize: BoardSize, private val customImages: List<String>?){

    // Declare properties for storing the memory cards and tracking the number of pairs found.
    val cards: List<MemoryCard>
    var numPairsFound = 0

    // Declare private properties to track the number of card flips and the index of a single selected card.
    private var numCardFlips = 0
    private var indexOfSingleSelectedCard: Int ? = null

    // Initialize the MemoryGame class.
    init {
        // Check if customImages is null.
        if (customImages == null){
            // If customImages is null, use default icons and shuffle them. Create a list of MemoryCard instances.
            val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
            val randomizedImages = (chosenImages + chosenImages).shuffled()
            cards = randomizedImages.map { MemoryCard(it) }
        } else {
            // If customImages is not null, use custom images and shuffle them. Create a list of MemoryCard instances.
            val randomizedImages = (customImages + customImages).shuffled()
            cards = randomizedImages.map { MemoryCard(it.hashCode(), it) }
        }
    }

    // Define a function to flip a card at the specified position and return whether a match was found.
    fun flipCard(position: Int): Boolean {
        // Increment the number of card flips.
        numCardFlips++
        // Get the MemoryCard instance at the specified position.
        val card = cards[position]

        // Three cases for card flipping logic:
        // 0 cards previously flipped over => restore cards + flip over the selected card
        // 1 card previously flipped over => flip over the selected card + check if the images match
        // 2 cards previously flipped over => restore cards + flip over the selected card
        var foundMatch = false
        if(indexOfSingleSelectedCard == null){
            // 0 or 2 cards flipped over
            restoreCards()
            indexOfSingleSelectedCard = position
        } else {
            // 1 card flipped over
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
        }

        // Flip the selected card.
        card.isFaceUp = !card.isFaceUp
        // Return whether a match was found.
        return foundMatch
    }

    // Define a function to check if two flipped cards match based on their identifiers.
    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        // If the identifiers of the two cards do not match, return false.
        if(cards[position1].identifier != cards[position2].identifier){
            return false
        }
        // Mark both cards as matched and increment the number of pairs found.
        cards[position1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
        // Return true, indicating a match was found.
        return true
    }

    // Define a function to restore cards that are not matched by setting their face-up state to false.
    private fun restoreCards() {
        for (card in cards){
            // If the card is not matched, set its face-up state to false.
            if(!card.isMatched){
                card.isFaceUp = false
            }
        }
    }

    // Define a function to check if the player has won the game by matching all pairs.
    fun haveWonGame(): Boolean {
        // Return true if the number of pairs found equals the total number of pairs in the game.
        return numPairsFound == boardSize.getNumPairs()
    }

    // Define a function to check if a card at the specified position is face-up.
    fun isCardFaceUp(position: Int): Boolean {
        // Return the face-up state of the card at the specified position.
        return cards[position].isFaceUp
    }

    // Define a function to get the number of moves (card flips) made by the player.
    fun getNumMoves(): Int {
        // Return the total number of card flips divided by 2 (each pair of flips counts as one move).
        return numCardFlips / 2
    }
}
