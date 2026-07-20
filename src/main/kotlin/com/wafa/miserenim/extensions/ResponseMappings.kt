package com.wafa.miserenim.extensions

import com.wafa.miserenim.domain.model.Game
import com.wafa.miserenim.domain.model.Move
import com.wafa.miserenim.dto.GameResponse
import com.wafa.miserenim.dto.MoveResponse

internal fun Game.toResponse(): GameResponse =
    GameResponse(
        id = id,
        initialHeapSize = initialHeapSize,
        currentHeapSize = currentHeapSize,
        currentTurn = currentTurn,
        gameState = gameState,
        winner = winner,
        strategy = strategy,
    )

internal fun Move.toResponse(): MoveResponse =
    MoveResponse(
        player = player,
        matchesRemoved = matchesRemoved,
    )
