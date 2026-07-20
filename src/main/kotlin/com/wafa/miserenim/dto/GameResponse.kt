package com.wafa.miserenim.dto

import com.wafa.miserenim.domain.model.GameState
import com.wafa.miserenim.domain.model.Player
import com.wafa.miserenim.domain.model.StrategyType
import java.util.UUID

data class GameResponse(
    val id: UUID,
    val initialHeapSize: Int,
    val currentHeapSize: Int,
    val currentTurn: Player?,
    val gameState: GameState,
    val winner: Player?,
    val strategy: StrategyType,
)
