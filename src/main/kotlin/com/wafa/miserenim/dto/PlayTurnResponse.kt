package com.wafa.miserenim.dto

data class PlayTurnResponse(
    val moves: List<MoveResponse>,
    val game: GameResponse,
)
