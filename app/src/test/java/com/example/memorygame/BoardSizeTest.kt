package com.example.memorygame.models

import org.junit.Assert.assertEquals
import org.junit.Test

class BoardSizeTest {

    @Test
    fun testGetWidth() {
        assertEquals(2, BoardSize.EASY.getWidth())
        assertEquals(3, BoardSize.MEDIUM.getWidth())
        assertEquals(4, BoardSize.HARD.getWidth())
    }

    @Test
    fun testGetHeight() {
        assertEquals(4, BoardSize.EASY.getHeight()) // 8 cards, width 2
        assertEquals(6, BoardSize.MEDIUM.getHeight()) // 18 cards, width 3
        assertEquals(6, BoardSize.HARD.getHeight()) // 24 cards, width 4
    }

    @Test
    fun testGetNumPairs() {
        assertEquals(4, BoardSize.EASY.getNumPairs()) // 8 cards total
        assertEquals(9, BoardSize.MEDIUM.getNumPairs()) // 18 cards total
        assertEquals(12, BoardSize.HARD.getNumPairs()) // 24 cards total
    }

    @Test
    fun testGetByValue() {
        assertEquals(BoardSize.EASY, BoardSize.getByValue(8))
        assertEquals(BoardSize.MEDIUM, BoardSize.getByValue(18))
        assertEquals(BoardSize.HARD, BoardSize.getByValue(24))
    }
}
