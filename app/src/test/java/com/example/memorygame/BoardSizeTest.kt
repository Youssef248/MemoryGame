package com.example.memorygame.models

import org.junit.Assert.assertEquals
import org.junit.Test


class BoardSizeTest {

    @Test
    fun testGetWidth() {
        // Check if the `getWidth()` function of `BoardSize` returns the expected values for each difficulty level.
        assertEquals(2, BoardSize.EASY.getWidth())
        assertEquals(3, BoardSize.MEDIUM.getWidth())
        assertEquals(4, BoardSize.HARD.getWidth())
    }

    @Test
    fun testGetHeight() {
        // Check if the `getHeight()` function of `BoardSize` returns the expected values for each difficulty level.
        assertEquals(4, BoardSize.EASY.getHeight()) // 8 cards, width 2
        assertEquals(6, BoardSize.MEDIUM.getHeight()) // 18 cards, width 3
        assertEquals(6, BoardSize.HARD.getHeight()) // 24 cards, width 4
    }

    @Test
    fun testGetNumPairs() {
        // Check if the `getNumPairs()` function of `BoardSize` returns the expected values for each difficulty level.
        assertEquals(4, BoardSize.EASY.getNumPairs()) // 8 cards total
        assertEquals(9, BoardSize.MEDIUM.getNumPairs()) // 18 cards total
        assertEquals(12, BoardSize.HARD.getNumPairs()) // 24 cards total
    }

    @Test
    fun testGetByValue() {
        // Check if the `getByValue()` function of `BoardSize` returns the expected `BoardSize` enum for given values.
        assertEquals(BoardSize.EASY, BoardSize.getByValue(8))
        assertEquals(BoardSize.MEDIUM, BoardSize.getByValue(18))
        assertEquals(BoardSize.HARD, BoardSize.getByValue(24))
    }
}
