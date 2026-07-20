package com.wafa.miserenim.service

import com.wafa.miserenim.domain.model.Game
import com.wafa.miserenim.domain.model.GameState
import com.wafa.miserenim.domain.model.Player
import com.wafa.miserenim.domain.model.StrategyType
import com.wafa.miserenim.domain.model.TurnResult
import com.wafa.miserenim.domain.strategy.ComputerStrategy
import com.wafa.miserenim.domain.strategy.OptimalComputerStrategy
import com.wafa.miserenim.domain.strategy.RandomComputerStrategy
import com.wafa.miserenim.repository.GameRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GameService private constructor(
    private val gameRepository: GameRepository,
    private val strategies: Map<StrategyType, ComputerStrategy>,
) {
    @Autowired
    constructor(gameRepository: GameRepository) : this(
        gameRepository = gameRepository,
        optimalStrategy = OptimalComputerStrategy(),
        randomStrategy = RandomComputerStrategy(),
    )

    internal constructor(
        gameRepository: GameRepository,
        optimalStrategy: ComputerStrategy,
        randomStrategy: ComputerStrategy,
    ) : this(
        gameRepository = gameRepository,
        strategies = mapOf(
            StrategyType.OPTIMAL to optimalStrategy,
            StrategyType.RANDOM to randomStrategy,
        ),
    )

    fun startGame(
        initialHeapSize: Int,
        strategy: StrategyType = StrategyType.OPTIMAL,
    ): Game {
        val game = Game(initialHeapSize = initialHeapSize, strategy = strategy)
        return gameRepository.save(game)
    }

    fun getGame(gameId: UUID): Game =
        gameRepository.findById(gameId) ?: throw GameNotFoundException(gameId)

    fun playTurn(gameId: UUID, matchesToRemove: Int): TurnResult {
        val game = getGame(gameId)

        return synchronized(game) {
            check(game.gameState == GameState.GAME_ON) { "Cannot play a turn after the game is over" }
            check(game.currentTurn == Player.HUMAN) { "Game is not waiting for a human move" }

            val humanMove = game.applyMove(matchesToRemove)
            val moves =
                if (game.gameState == GameState.GAME_OVER) {
                    listOf(humanMove)
                } else {
                    val strategy = strategies.getValue(game.strategy)
                    val computerMove = game.applyMove(strategy.chooseMove(game.currentHeapSize))
                    listOf(humanMove, computerMove)
                }

            gameRepository.save(game)
            TurnResult(moves = moves, game = game)
        }
    }
}
