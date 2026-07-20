package com.wafa.miserenim.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class MakeMoveRequest(
    @field:Min(value = 1, message = "matchesToRemove must be between 1 and 3")
    @field:Max(value = 3, message = "matchesToRemove must be between 1 and 3")
    val matchesToRemove: Int,
)
