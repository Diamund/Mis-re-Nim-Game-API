package com.wafa.miserenim.dto

import com.wafa.miserenim.domain.model.StrategyType
import jakarta.validation.constraints.Positive

data class CreateGameRequest(
    @field:Positive(message = "initialHeapSize must be positive")
    val initialHeapSize: Int,
    val strategy: StrategyType = StrategyType.OPTIMAL,
)
