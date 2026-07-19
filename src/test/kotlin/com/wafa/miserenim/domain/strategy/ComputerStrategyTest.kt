package com.wafa.miserenim.domain.strategy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.random.Random

class ComputerStrategyTest {

    private val optimalStrategy = OptimalComputerStrategy()

    @ParameterizedTest
    @CsvSource(
        "1, 1",
        "2, 1",
        "3, 2",
        "4, 3",
        "5, 1",
        "6, 1",
        "7, 2",
        "8, 3",
        "9, 1",
    )
    fun `optimal strategy chooses the expected move`(currentHeapSize: Int, expectedMove: Int) {
        assertEquals(expectedMove, optimalStrategy.chooseMove(currentHeapSize))
    }

    @Test
    fun `optimal strategy leaves a losing heap when a winning move exists`() {
        (2..100)
            .filter { it % 4 != 1 }
            .forEach { currentHeapSize ->
                val move = optimalStrategy.chooseMove(currentHeapSize)

                assertEquals(1, (currentHeapSize - move) % 4)
                assertTrue(move in 1..minOf(3, currentHeapSize))
            }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -1])
    fun `optimal strategy rejects a non-positive heap size`(currentHeapSize: Int) {
        assertThrows(IllegalArgumentException::class.java) {
            optimalStrategy.chooseMove(currentHeapSize)
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 10])
    fun `random strategy always chooses a legal move`(currentHeapSize: Int) {
        val strategy = RandomComputerStrategy(Random(currentHeapSize))

        repeat(100) {
            val move = strategy.chooseMove(currentHeapSize)

            assertTrue(move in 1..minOf(3, currentHeapSize))
        }
    }

    @Test
    fun `random strategy is reproducible with a seeded generator`() {
        val firstStrategy = RandomComputerStrategy(Random(42))
        val secondStrategy = RandomComputerStrategy(Random(42))

        val firstMoves = List(20) { firstStrategy.chooseMove(10) }
        val secondMoves = List(20) { secondStrategy.chooseMove(10) }

        assertEquals(firstMoves, secondMoves)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -1])
    fun `random strategy rejects a non-positive heap size`(currentHeapSize: Int) {
        val strategy = RandomComputerStrategy(Random(42))

        assertThrows(IllegalArgumentException::class.java) {
            strategy.chooseMove(currentHeapSize)
        }
    }
}
