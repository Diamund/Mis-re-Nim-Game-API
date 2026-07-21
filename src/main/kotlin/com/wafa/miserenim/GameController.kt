package com.wafa.miserenim

import com.wafa.miserenim.domain.model.Move
import com.wafa.miserenim.dto.CreateGameRequest
import com.wafa.miserenim.dto.ErrorResponse
import com.wafa.miserenim.dto.GameResponse
import com.wafa.miserenim.dto.MakeMoveRequest
import com.wafa.miserenim.dto.PlayTurnResponse
import com.wafa.miserenim.extensions.toResponse
import com.wafa.miserenim.service.GameService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
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
    @Operation(summary = "Create the game")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "201",
                    description = "Game created",
                    content =
                        [
                            Content(
                                mediaType = "application/json",
                                schema = Schema(implementation = GameResponse::class),
                            ),
                        ],
                ),
                ApiResponse(
                    responseCode = "400",
                    description = "Invalid game creation request",
                    content =
                        [
                            Content(
                                mediaType = "application/json",
                                schema = Schema(implementation = ErrorResponse::class),
                            ),
                        ],
                ),
            ],
    )
    @PostMapping
    fun createGame(
        @Valid @RequestBody request: CreateGameRequest,
    ): ResponseEntity<GameResponse> {
        val game = gameService.startGame(request.initialHeapSize, request.strategy)

        return ResponseEntity
            .created(URI.create("/games/${game.id}"))
            .body(game.toResponse())
    }

    @Operation(summary = "Get the game")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Game found",
                    content =
                        [
                            Content(
                                mediaType = "application/json",
                                schema = Schema(implementation = GameResponse::class),
                            ),
                        ],
                ),
                ApiResponse(
                    responseCode = "400",
                    description = "Invalid game ID",
                    content =
                        [
                            Content(
                                mediaType = "application/json",
                                schema = Schema(implementation = ErrorResponse::class),
                            ),
                        ],
                ),
                ApiResponse(
                    responseCode = "404",
                    description = "Game not found",
                    content =
                        [
                            Content(
                                mediaType = "application/json",
                                schema = Schema(implementation = ErrorResponse::class),
                            ),
                        ],
                ),
            ],
    )
    @GetMapping("/{gameId}")
    fun getGame(
        @PathVariable gameId: UUID,
    ): GameResponse = gameService.getGame(gameId).toResponse()

    @Operation(summary = "play the game")
    @ApiResponses(
        value =
            [
                ApiResponse(
                    responseCode = "200",
                    description = "Turn played",
                    content =
                        [
                            Content(
                                mediaType = "application/json",
                                schema = Schema(implementation = PlayTurnResponse::class),
                            ),
                        ],
                ),
                ApiResponse(
                    responseCode = "400",
                    description = "Invalid game ID or move request",
                    content =
                        [
                            Content(
                                mediaType = "application/json",
                                schema = Schema(implementation = ErrorResponse::class),
                            ),
                        ],
                ),
                ApiResponse(
                    responseCode = "404",
                    description = "Game not found",
                    content =
                        [
                            Content(
                                mediaType = "application/json",
                                schema = Schema(implementation = ErrorResponse::class),
                            ),
                        ],
                ),
                ApiResponse(
                    responseCode = "409",
                    description = "Game cannot accept a human move",
                    content =
                        [
                            Content(
                                mediaType = "application/json",
                                schema = Schema(implementation = ErrorResponse::class),
                            ),
                        ],
                ),
            ],
    )
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
