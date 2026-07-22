package com.wafa.miserenim

import com.wafa.miserenim.api.GameApi
import com.wafa.miserenim.domain.model.Move
import com.wafa.miserenim.dto.CreateGameRequest
import com.wafa.miserenim.dto.GameResponse
import com.wafa.miserenim.dto.MakeMoveRequest
import com.wafa.miserenim.dto.PlayTurnResponse
import com.wafa.miserenim.extensions.toResponse
import com.wafa.miserenim.service.GameService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
class GameController(
    private val gameService: GameService,
) : GameApi {
    override fun createGame(request: CreateGameRequest): ResponseEntity<GameResponse> {
        val game = gameService.startGame(request.initialHeapSize, request.strategy)

        return ResponseEntity
            .created(URI.create("/games/${game.id}"))
            .body(game.toResponse())
    }

    override fun getGame(gameId: UUID): GameResponse = gameService.getGame(gameId).toResponse()

    override fun playTurn(gameId: UUID, request: MakeMoveRequest): PlayTurnResponse {
        val result = gameService.playTurn(gameId, request.matchesToRemove)

        return PlayTurnResponse(
            moves = result.moves.map(Move::toResponse),
            game = result.game.toResponse(),
        )
    }
}
