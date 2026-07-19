package com.wafa.miserenim.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class GameTest {

    @Test
    fun `creates a game with the expected initial state and default strategy`() {
        val game = Game(initialHeapSize = 10)

        assertNotNull(game.id)
        assertEquals(10, game.initialHeapSize)
        assertEquals(10, game.currentHeapSize)
        assertEquals(Player.HUMAN, game.currentTurn)
        assertEquals(GameState.GAME_ON, game.gameState)
        assertNull(game.winner)
        assertEquals(StrategyType.OPTIMAL, game.strategy)
    }

    @Test
    fun `uses the selected strategy for the lifetime of the game`() {
        val game = Game(initialHeapSize = 10, strategy = StrategyType.RANDOM)

        game.applyMove(1)

        assertEquals(StrategyType.RANDOM, game.strategy)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -1])
    fun `rejects a non-positive initial heap size`(initialHeapSize: Int) {
        assertThrows(IllegalArgumentException::class.java) {
            Game(initialHeapSize = initialHeapSize)
        }
    }

    @Test
    fun `applies boundary moves returns them and alternates turns`() {
        val game = Game(initialHeapSize = 10)

        val humanMove = game.applyMove(1)
        val computerMove = game.applyMove(3)

        assertEquals(Move(Player.HUMAN, 1), humanMove)
        assertEquals(Move(Player.COMPUTER, 3), computerMove)
        assertEquals(6, game.currentHeapSize)
        assertEquals(10, game.initialHeapSize)
        assertEquals(Player.HUMAN, game.currentTurn)
        assertEquals(GameState.GAME_ON, game.gameState)
        assertNull(game.winner)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 4])
    fun `rejects moves outside the allowed range without changing state`(matchesToRemove: Int) {
        val game = Game(initialHeapSize = 5)

        assertThrows(IllegalArgumentException::class.java) {
            game.applyMove(matchesToRemove)
        }

        assertEquals(5, game.currentHeapSize)
        assertEquals(Player.HUMAN, game.currentTurn)
        assertEquals(GameState.GAME_ON, game.gameState)
        assertNull(game.winner)
    }

    @Test
    fun `rejects removing more matchsticks than remain without changing state`() {
        val game = Game(initialHeapSize = 2)

        assertThrows(IllegalArgumentException::class.java) {
            game.applyMove(3)
        }

        assertEquals(2, game.currentHeapSize)
        assertEquals(Player.HUMAN, game.currentTurn)
        assertEquals(GameState.GAME_ON, game.gameState)
        assertNull(game.winner)
    }

    @Test
    fun `human loses after removing the final matchstick`() {
        val game = Game(initialHeapSize = 1)

        val move = game.applyMove(1)

        assertEquals(Move(Player.HUMAN, 1), move)
        assertEquals(0, game.currentHeapSize)
        assertEquals(GameState.GAME_OVER, game.gameState)
        assertNull(game.currentTurn)
        assertEquals(Player.COMPUTER, game.winner)
    }

    @Test
    fun `computer loses after removing the final matchstick`() {
        val game = Game(initialHeapSize = 2)
        game.applyMove(1)

        val move = game.applyMove(1)

        assertEquals(Move(Player.COMPUTER, 1), move)
        assertEquals(0, game.currentHeapSize)
        assertEquals(GameState.GAME_OVER, game.gameState)
        assertNull(game.currentTurn)
        assertEquals(Player.HUMAN, game.winner)
    }

    @Test
    fun `rejects moves after the game is over without changing state`() {
        val game = Game(initialHeapSize = 1)
        game.applyMove(1)

        assertThrows(IllegalStateException::class.java) {
            game.applyMove(1)
        }

        assertEquals(0, game.currentHeapSize)
        assertEquals(GameState.GAME_OVER, game.gameState)
        assertNull(game.currentTurn)
        assertEquals(Player.COMPUTER, game.winner)
    }
}
