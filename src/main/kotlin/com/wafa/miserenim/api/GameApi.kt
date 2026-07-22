package com.wafa.miserenim.api

import com.wafa.miserenim.dto.CreateGameRequest
import com.wafa.miserenim.dto.ErrorResponse
import com.wafa.miserenim.dto.GameResponse
import com.wafa.miserenim.dto.MakeMoveRequest
import com.wafa.miserenim.dto.PlayTurnResponse
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
import java.util.UUID

@RequestMapping("/games")
interface GameApi {
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
    ): ResponseEntity<GameResponse>

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
    ): GameResponse

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
    ): PlayTurnResponse
}
