package com.wafa.miserenim.dto

import com.wafa.miserenim.domain.model.Player

data class MoveResponse(
    val player: Player,
    val matchesRemoved: Int,
)
