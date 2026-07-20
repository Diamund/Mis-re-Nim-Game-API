package com.wafa.miserenim.service

import com.wafa.miserenim.domain.model.Game
import com.wafa.miserenim.domain.model.GameState
import com.wafa.miserenim.domain.model.Move
import com.wafa.miserenim.domain.model.Player
import com.wafa.miserenim.domain.model.StrategyType
import com.wafa.miserenim.domain.strategy.ComputerStrategy
import com.wafa.miserenim.repository.GameRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.UUID

class GameServiceTest {
    private val repository = RecordingGameRepository()
    private val optimalStrategy = RecordingComputerStrategy(matchesToRemove = 1)
    private val randomStrategy = RecordingComputerStrategy(matchesToRemove = 3)
    private val service = GameService(repository, optimalStrategy, randomStrategy)

    @Test
    fun `starts and stores a game with the default optimal strategy`() {
        val game = service.startGame(initialHeapSize = 10)

        assertEquals(StrategyType.OPTIMAL, game.strategy)
        assertEquals(10, game.initialHeapSize)
        assertSame(game, repository.findById(game.id))
        assertEquals(1, repository.saveCount)
    }

    @Test
    fun `starts and stores a game with the selected strategy`() {
        val game = service.startGame(initialHeapSize = 10, strategy = StrategyType.RANDOM)

        assertEquals(StrategyType.RANDOM, game.strategy)
        assertSame(game, repository.findById(game.id))
    }

    @Test
    fun `starts games with distinct identities and independent strategies`() {
        val optimalGame = service.startGame(initialHeapSize = 10)
        val randomGame = service.startGame(initialHeapSize = 10, strategy = StrategyType.RANDOM)

        assertNotEquals(optimalGame.id, randomGame.id)
        assertEquals(StrategyType.OPTIMAL, optimalGame.strategy)
        assertEquals(StrategyType.RANDOM, randomGame.strategy)
        assertSame(optimalGame, repository.findById(optimalGame.id))
        assertSame(randomGame, repository.findById(randomGame.id))
    }

    @Test
    fun `retrieves an existing game`() {
        val game = service.startGame(initialHeapSize = 10)

        assertSame(game, service.getGame(game.id))
    }

    @Test
    fun `reports an unknown game for retrieval and turn play`() {
        val gameId = UUID.randomUUID()

        val retrievalException = assertThrows(GameNotFoundException::class.java) {
            service.getGame(gameId)
        }
        val turnException = assertThrows(GameNotFoundException::class.java) {
            service.playTurn(gameId, matchesToRemove = 1)
        }

        assertEquals(gameId, retrievalException.gameId)
        assertEquals(gameId, turnException.gameId)
    }

    @Test
    fun `coordinates a normal human and computer turn`() {
        val game = service.startGame(initialHeapSize = 10)

        val result = service.playTurn(game.id, matchesToRemove = 2)

        assertSame(game, result.game)
        assertEquals(7, game.currentHeapSize)
        assertEquals(Player.HUMAN, game.currentTurn)
        assertEquals(GameState.GAME_ON, game.gameState)
        assertNull(game.winner)
        assertEquals(listOf(8), optimalStrategy.requestedHeapSizes)
        assertEquals(2, repository.saveCount)
        assertSame(game, repository.findById(game.id))
    }

    @Test
    fun `uses the strategy stored on the specific game`() {
        val game = service.startGame(initialHeapSize = 10, strategy = StrategyType.RANDOM)

        val result = service.playTurn(game.id, matchesToRemove = 1)

        assertEquals(Move(Player.COMPUTER, 3), result.moves.last())
        assertEquals(emptyList<Int>(), optimalStrategy.requestedHeapSizes)
        assertEquals(listOf(9), randomStrategy.requestedHeapSizes)
    }

    @Test
    fun `returns human and computer moves in performed order`() {
        val game = service.startGame(initialHeapSize = 10)

        val result = service.playTurn(game.id, matchesToRemove = 2)

        assertEquals(
            listOf(
                Move(Player.HUMAN, 2),
                Move(Player.COMPUTER, 1),
            ),
            result.moves,
        )
    }

    @Test
    fun `returns only the human move when the human takes the final matchstick`() {
        val game = service.startGame(initialHeapSize = 1)

        val result = service.playTurn(game.id, matchesToRemove = 1)

        assertEquals(listOf(Move(Player.HUMAN, 1)), result.moves)
        assertEquals(0, game.currentHeapSize)
        assertEquals(GameState.GAME_OVER, game.gameState)
        assertNull(game.currentTurn)
        assertEquals(Player.COMPUTER, game.winner)
        assertEquals(emptyList<Int>(), optimalStrategy.requestedHeapSizes)
        assertEquals(2, repository.saveCount)
    }

    @Test
    fun `returns both moves when the computer takes the final matchstick`() {
        val game = service.startGame(initialHeapSize = 2)

        val result = service.playTurn(game.id, matchesToRemove = 1)

        assertEquals(
            listOf(
                Move(Player.HUMAN, 1),
                Move(Player.COMPUTER, 1),
            ),
            result.moves,
        )
        assertEquals(0, game.currentHeapSize)
        assertEquals(GameState.GAME_OVER, game.gameState)
        assertNull(game.currentTurn)
        assertEquals(Player.HUMAN, game.winner)
        assertEquals(2, repository.saveCount)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 3, 4])
    fun `invalid human moves leave the game unchanged`(matchesToRemove: Int) {
        val game = service.startGame(initialHeapSize = 2)

        assertThrows(IllegalArgumentException::class.java) {
            service.playTurn(game.id, matchesToRemove)
        }

        assertEquals(2, game.currentHeapSize)
        assertEquals(Player.HUMAN, game.currentTurn)
        assertEquals(GameState.GAME_ON, game.gameState)
        assertNull(game.winner)
        assertEquals(emptyList<Int>(), optimalStrategy.requestedHeapSizes)
        assertEquals(1, repository.saveCount)
    }

    @Test
    fun `rejects a move after game completion without changing the game`() {
        val game = service.startGame(initialHeapSize = 1)
        service.playTurn(game.id, matchesToRemove = 1)

        assertThrows(IllegalStateException::class.java) {
            service.playTurn(game.id, matchesToRemove = 1)
        }

        assertEquals(0, game.currentHeapSize)
        assertEquals(GameState.GAME_OVER, game.gameState)
        assertNull(game.currentTurn)
        assertEquals(Player.COMPUTER, game.winner)
        assertEquals(2, repository.saveCount)
    }

    @Test
    fun `rejects a turn when the game expects the computer`() {
        val game = service.startGame(initialHeapSize = 5)
        game.applyMove(1)

        assertThrows(IllegalStateException::class.java) {
            service.playTurn(game.id, matchesToRemove = 1)
        }

        assertEquals(4, game.currentHeapSize)
        assertEquals(Player.COMPUTER, game.currentTurn)
        assertEquals(GameState.GAME_ON, game.gameState)
        assertNull(game.winner)
        assertEquals(emptyList<Int>(), optimalStrategy.requestedHeapSizes)
        assertEquals(1, repository.saveCount)
    }

    private class RecordingGameRepository : GameRepository {
        private val games = mutableMapOf<UUID, Game>()

        var saveCount: Int = 0
            private set

        override fun save(game: Game): Game {
            games[game.id] = game
            saveCount++
            return game
        }

        override fun findById(id: UUID): Game? = games[id]
    }

    private class RecordingComputerStrategy(
        private val matchesToRemove: Int,
    ) : ComputerStrategy {
        val requestedHeapSizes = mutableListOf<Int>()

        override fun chooseMove(currentHeapSize: Int): Int {
            requestedHeapSizes += currentHeapSize
            return matchesToRemove
        }
    }
}
