package com.wafa.miserenim.repository

import com.wafa.miserenim.domain.model.Game
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.util.UUID

class InMemoryGameRepositoryTest {

    @Test
    fun `saves and retrieves a game`() {
        val repository: GameRepository = InMemoryGameRepository()
        val game = Game(initialHeapSize = 10)

        repository.save(game)

        assertSame(game, repository.findById(game.id))
    }

    @Test
    fun `returns null for an unknown id`() {
        val repository: GameRepository = InMemoryGameRepository()

        assertNull(repository.findById(UUID.randomUUID()))
    }

    @Test
    fun `keeps multiple games independent`() {
        val repository: GameRepository = InMemoryGameRepository()
        val firstGame = Game(initialHeapSize = 10)
        val secondGame = Game(initialHeapSize = 20)

        repository.save(firstGame)
        repository.save(secondGame)

        assertSame(firstGame, repository.findById(firstGame.id))
        assertSame(secondGame, repository.findById(secondGame.id))
    }

    @Test
    fun `returns the saved game from save`() {
        val repository: GameRepository = InMemoryGameRepository()
        val game = Game(initialHeapSize = 10)

        val savedGame = repository.save(game)

        assertSame(game, savedGame)
    }
}
