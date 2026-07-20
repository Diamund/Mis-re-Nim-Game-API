package com.wafa.miserenim

import com.wafa.miserenim.domain.model.Game
import com.wafa.miserenim.domain.model.StrategyType
import com.wafa.miserenim.domain.model.TurnResult
import com.wafa.miserenim.service.GameNotFoundException
import com.wafa.miserenim.service.GameService
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(GameController::class)
class GameControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var gameService: GameService

    private val gameId: UUID = UUID.fromString("2db4f435-2e8d-477f-a31a-29aa52c712f0")

    @Test
    fun `creates a game with the default strategy`() {
        val game = Game(initialHeapSize = 13, id = gameId)
        Mockito.`when`(gameService.startGame(13, StrategyType.OPTIMAL)).thenReturn(game)

        mockMvc
            .perform(
                post("/games")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"initialHeapSize":13}"""),
            ).andExpect(status().isCreated)
            .andExpect(header().string("Location", "/games/$gameId"))
            .andExpect(jsonPath("$.id").value(gameId.toString()))
            .andExpect(jsonPath("$.initialHeapSize").value(13))
            .andExpect(jsonPath("$.currentHeapSize").value(13))
            .andExpect(jsonPath("$.currentTurn").value("HUMAN"))
            .andExpect(jsonPath("$.gameState").value("GAME_ON"))
            .andExpect(jsonPath("$.winner").value(nullValue()))
            .andExpect(jsonPath("$.strategy").value("OPTIMAL"))

        Mockito.verify(gameService).startGame(13, StrategyType.OPTIMAL)
    }

    @Test
    fun `creates a game with an explicitly selected strategy`() {
        val game = Game(initialHeapSize = 13, strategy = StrategyType.RANDOM, id = gameId)
        Mockito.`when`(gameService.startGame(13, StrategyType.RANDOM)).thenReturn(game)

        mockMvc
            .perform(
                post("/games")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"initialHeapSize":13,"strategy":"RANDOM"}"""),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.strategy").value("RANDOM"))

        Mockito.verify(gameService).startGame(13, StrategyType.RANDOM)
    }

    @Test
    fun `retrieves an existing game`() {
        val game = Game(initialHeapSize = 13, strategy = StrategyType.RANDOM, id = gameId)
        Mockito.`when`(gameService.getGame(gameId)).thenReturn(game)

        mockMvc
            .perform(get("/games/{gameId}", gameId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(gameId.toString()))
            .andExpect(jsonPath("$.initialHeapSize").value(13))
            .andExpect(jsonPath("$.currentHeapSize").value(13))
            .andExpect(jsonPath("$.currentTurn").value("HUMAN"))
            .andExpect(jsonPath("$.gameState").value("GAME_ON"))
            .andExpect(jsonPath("$.winner").value(nullValue()))
            .andExpect(jsonPath("$.strategy").value("RANDOM"))

        Mockito.verify(gameService).getGame(gameId)
    }

    @Test
    fun `returns not found for an unknown game`() {
        Mockito.`when`(gameService.getGame(gameId)).thenThrow(GameNotFoundException(gameId))

        mockMvc
            .perform(get("/games/{gameId}", gameId))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Game not found: $gameId"))
            .andExpect(jsonPath("$.length()").value(1))
    }

    @Test
    fun `returns bad request for an invalid game id`() {
        mockMvc
            .perform(get("/games/not-a-uuid"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Invalid gameId"))

        Mockito.verifyNoInteractions(gameService)
    }

    @Test
    fun `returns human and computer moves in execution order`() {
        val game = Game(initialHeapSize = 10, id = gameId)
        val humanMove = game.applyMove(2)
        val computerMove = game.applyMove(3)
        Mockito
            .`when`(gameService.playTurn(gameId, 2))
            .thenReturn(TurnResult(moves = listOf(humanMove, computerMove), game = game))

        mockMvc
            .perform(
                post("/games/{gameId}/moves", gameId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"matchesToRemove":2}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.moves.length()").value(2))
            .andExpect(jsonPath("$.moves[0].player").value("HUMAN"))
            .andExpect(jsonPath("$.moves[0].matchesRemoved").value(2))
            .andExpect(jsonPath("$.moves[1].player").value("COMPUTER"))
            .andExpect(jsonPath("$.moves[1].matchesRemoved").value(3))
            .andExpect(jsonPath("$.game.id").value(gameId.toString()))
            .andExpect(jsonPath("$.game.currentHeapSize").value(5))
            .andExpect(jsonPath("$.game.currentTurn").value("HUMAN"))
            .andExpect(jsonPath("$.game.gameState").value("GAME_ON"))

        Mockito.verify(gameService).playTurn(gameId, 2)
    }

    @Test
    fun `returns one move and the final game when the human ends the game`() {
        val game = Game(initialHeapSize = 1, id = gameId)
        val humanMove = game.applyMove(1)
        Mockito
            .`when`(gameService.playTurn(gameId, 1))
            .thenReturn(TurnResult(moves = listOf(humanMove), game = game))

        mockMvc
            .perform(
                post("/games/{gameId}/moves", gameId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"matchesToRemove":1}"""),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.moves.length()").value(1))
            .andExpect(jsonPath("$.moves[0].player").value("HUMAN"))
            .andExpect(jsonPath("$.moves[0].matchesRemoved").value(1))
            .andExpect(jsonPath("$.game.id").value(gameId.toString()))
            .andExpect(jsonPath("$.game.initialHeapSize").value(1))
            .andExpect(jsonPath("$.game.currentHeapSize").value(0))
            .andExpect(jsonPath("$.game.currentTurn").value(nullValue()))
            .andExpect(jsonPath("$.game.gameState").value("GAME_OVER"))
            .andExpect(jsonPath("$.game.winner").value("COMPUTER"))
            .andExpect(jsonPath("$.game.strategy").value("OPTIMAL"))

        Mockito.verify(gameService).playTurn(gameId, 1)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -1])
    fun `rejects non-positive initial heap sizes without starting a game`(initialHeapSize: Int) {
        mockMvc
            .perform(
                post("/games")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"initialHeapSize":$initialHeapSize}"""),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("initialHeapSize must be positive"))

        Mockito.verifyNoInteractions(gameService)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 4])
    fun `rejects moves outside the allowed range without playing a turn`(matchesToRemove: Int) {
        mockMvc
            .perform(
                post("/games/{gameId}/moves", gameId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"matchesToRemove":$matchesToRemove}"""),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("matchesToRemove must be between 1 and 3"))

        Mockito.verifyNoInteractions(gameService)
    }

    @Test
    fun `rejects a strategy on a move request without playing a turn`() {
        mockMvc
            .perform(
                post("/games/{gameId}/moves", gameId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"matchesToRemove":1,"strategy":"RANDOM"}"""),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Malformed request body"))
            .andExpect(jsonPath("$.length()").value(1))

        Mockito.verifyNoInteractions(gameService)
    }

    @Test
    fun `returns bad request when more matchsticks are removed than remain`() {
        Mockito
            .`when`(gameService.playTurn(gameId, 3))
            .thenThrow(IllegalArgumentException("A move cannot remove more matchsticks than remain"))

        mockMvc
            .perform(
                post("/games/{gameId}/moves", gameId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"matchesToRemove":3}"""),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("A move cannot remove more matchsticks than remain"))
    }

    @Test
    fun `returns conflict when moving after game completion`() {
        Mockito
            .`when`(gameService.playTurn(gameId, 1))
            .thenThrow(IllegalStateException("Cannot play a turn after the game is over"))

        mockMvc
            .perform(
                post("/games/{gameId}/moves", gameId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"matchesToRemove":1}"""),
            ).andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("Cannot play a turn after the game is over"))
    }

    @Test
    fun `returns bad request for an invalid strategy`() {
        mockMvc
            .perform(
                post("/games")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"initialHeapSize":13,"strategy":"GREEDY"}"""),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Malformed request body"))

        Mockito.verifyNoInteractions(gameService)
    }

    @Test
    fun `returns bad request for malformed JSON`() {
        mockMvc
            .perform(
                post("/games")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"initialHeapSize":"""),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Malformed request body"))

        Mockito.verifyNoInteractions(gameService)
    }
}
