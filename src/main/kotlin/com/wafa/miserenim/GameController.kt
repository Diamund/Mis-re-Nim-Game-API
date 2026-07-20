package com.wafa.miserenim

import com.wafa.miserenim.domain.model.Move
import com.wafa.miserenim.dto.CreateGameRequest
import com.wafa.miserenim.dto.GameResponse
import com.wafa.miserenim.dto.MakeMoveRequest
import com.wafa.miserenim.dto.PlayTurnResponse
import com.wafa.miserenim.extensions.toResponse
import com.wafa.miserenim.service.GameService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/games")
class GameController(
    private val gameService: GameService,
) {
    @Operation(summary = "Create a game")
    @PostMapping
    fun createGame(
        @Valid @RequestBody request: CreateGameRequest,
    ): ResponseEntity<GameResponse> {
        val game = gameService.startGame(request.initialHeapSize, request.strategy)

        return ResponseEntity
            .created(URI.create("/games/${game.id}"))
            .body(game.toResponse())
    }

    @Operation(summary = "Get a game")
    @GetMapping("/{gameId}")
    fun getGame(
        @PathVariable gameId: UUID,
    ): GameResponse = gameService.getGame(gameId).toResponse()

    @Operation(summary = "Play a human move and, if the game continues, a computer move")
    @PostMapping("/{gameId}/moves")
    fun playTurn(
        @PathVariable gameId: UUID,
        @Valid @RequestBody request: MakeMoveRequest,
    ): PlayTurnResponse {
        val result = gameService.playTurn(gameId, request.matchesToRemove)

        return PlayTurnResponse(
            moves = result.moves.map(Move::toResponse),
            game = result.game.toResponse(),
        )
    }
}
