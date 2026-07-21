package com.wafa.miserenim.dto

import com.wafa.miserenim.domain.model.StrategyType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive

data class CreateGameRequest(
    @field:Schema(example = "13")
    @field:Positive(message = "initialHeapSize must be positive")
    val initialHeapSize: Int,
    @field:Schema(defaultValue = "OPTIMAL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val strategy: StrategyType = StrategyType.OPTIMAL,
)
