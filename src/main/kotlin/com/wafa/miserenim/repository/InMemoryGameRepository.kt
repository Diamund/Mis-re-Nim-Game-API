package com.wafa.miserenim.repository

import com.wafa.miserenim.domain.model.Game
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryGameRepository : GameRepository {
    private val games = ConcurrentHashMap<UUID, Game>()

    override fun save(game: Game): Game {
        games[game.id] = game
        return game
    }

    override fun findById(id: UUID): Game? = games[id]
}
