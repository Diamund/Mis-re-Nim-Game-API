package com.wafa.miserenim.repository

import com.wafa.miserenim.domain.model.Game
import java.util.UUID

interface GameRepository {
    fun save(game: Game): Game

    fun findById(id: UUID): Game?
}
