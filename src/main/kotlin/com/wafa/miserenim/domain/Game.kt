package com.wafa.miserenim.domain

import java.util.UUID

class Game(
    val initialHeapSize: Int,
    val strategy: StrategyType = StrategyType.OPTIMAL,
    val id: UUID = UUID.randomUUID(),
) {
    var currentHeapSize: Int = initialHeapSize
        private set

    var currentTurn: Player? = Player.HUMAN
        private set

    var gameState: GameState = GameState.GAME_ON
        private set

    var winner: Player? = null
        private set

    init {
        require(initialHeapSize > 0) { "Initial heap size must be positive" }
    }

    fun applyMove(matchesToRemove: Int): Move {
        check(gameState == GameState.GAME_ON) { "Cannot apply a move after the game is over" }
        require(matchesToRemove in 1..3) { "A move must remove between 1 and 3 matchsticks" }
        require(matchesToRemove <= currentHeapSize) { "A move cannot remove more matchsticks than remain" }

        val player = checkNotNull(currentTurn)
        currentHeapSize -= matchesToRemove

        if (currentHeapSize == 0) {
            gameState = GameState.GAME_OVER
            winner = player.opponent()
            currentTurn = null
        } else {
            currentTurn = player.opponent()
        }

        return Move(player, matchesToRemove)
    }

    private fun Player.opponent(): Player =
        when (this) {
            Player.HUMAN -> Player.COMPUTER
            Player.COMPUTER -> Player.HUMAN
        }
}
