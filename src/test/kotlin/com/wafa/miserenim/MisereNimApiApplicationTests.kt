package com.wafa.miserenim

import com.fasterxml.jackson.databind.JsonNode
import com.wafa.miserenim.domain.model.StrategyType
import com.wafa.miserenim.dto.CreateGameRequest
import com.wafa.miserenim.dto.MakeMoveRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MisereNimApiApplicationTests {
    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `supports deterministic gameplay`() {
        val createResponse =
            restTemplate.postForEntity(
                "/games",
                CreateGameRequest(initialHeapSize = 13, strategy = StrategyType.OPTIMAL),
                JsonNode::class.java,
            )

        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        val createdGame = requireBody(createResponse)
        val gameId = UUID.fromString(createdGame.path("id").asText())
        assertEquals(URI.create("/games/$gameId"), createResponse.headers.location)
        assertGameState(
            game = createdGame,
            gameId = gameId,
            currentHeapSize = 13,
            currentTurn = "HUMAN",
            gameState = "GAME_ON",
            winner = null,
        )

        val retrievedGame = getGame(gameId)
        assertEquals(createdGame, retrievedGame)

        playTurnAndAssert(
            gameId = gameId,
            humanMatchesRemoved = 2,
            computerMatchesRemoved = 2,
            expectedHeapSize = 9,
        )
        playTurnAndAssert(
            gameId = gameId,
            humanMatchesRemoved = 3,
            computerMatchesRemoved = 1,
            expectedHeapSize = 5,
        )
        playTurnAndAssert(
            gameId = gameId,
            humanMatchesRemoved = 1,
            computerMatchesRemoved = 3,
            expectedHeapSize = 1,
        )

        val finalTurn = playTurn(gameId, matchesToRemove = 1)
        val finalMoves = finalTurn.path("moves")
        assertEquals(1, finalMoves.size())
        assertMove(finalMoves[0], player = "HUMAN", matchesRemoved = 1)

        val finalGame = finalTurn.path("game")
        assertGameState(
            game = finalGame,
            gameId = gameId,
            currentHeapSize = 0,
            currentTurn = null,
            gameState = "GAME_OVER",
            winner = "COMPUTER",
        )
        assertEquals(finalGame, getGame(gameId))
    }

    @Test
    fun `exposes an accurate OpenAPI contract and Scalar UI`() {
        val openApiResponse = restTemplate.getForEntity("/v3/api-docs", JsonNode::class.java)

        assertEquals(HttpStatus.OK, openApiResponse.statusCode)
        val openApi = requireBody(openApiResponse)
        assertEquals("Misère Nim API", openApi.path("info").path("title").asText())
        assertEquals("v1", openApi.path("info").path("version").asText())

        val paths = openApi.path("paths")
        val createGameOperation = paths.path("/games").path("post")
        val getGameOperation = paths.path("/games/{gameId}").path("get")
        val playTurnOperation = paths.path("/games/{gameId}/moves").path("post")

        assertEquals(setOf("201", "400"), responseCodes(createGameOperation))
        assertEquals(setOf("200", "400", "404"), responseCodes(getGameOperation))
        assertEquals(setOf("200", "400", "404", "409"), responseCodes(playTurnOperation))
        assertErrorResponses(createGameOperation, "400")
        assertErrorResponses(getGameOperation, "400", "404")
        assertErrorResponses(playTurnOperation, "400", "404", "409")
        assertEquals("play the game", playTurnOperation.path("summary").asText())

        val createGameSchema = openApi.path("components").path("schemas").path("CreateGameRequest")
        assertEquals(setOf("initialHeapSize"), createGameSchema.path("required").map { it.asText() }.toSet())
        assertEquals(13, createGameSchema.path("properties").path("initialHeapSize").path("example").asInt())
        assertEquals("OPTIMAL", createGameSchema.path("properties").path("strategy").path("default").asText())

        val scalarResponse = restTemplate.getForEntity("/scalar", String::class.java)
        assertEquals(HttpStatus.OK, scalarResponse.statusCode)
    }

    private fun responseCodes(operation: JsonNode): Set<String> =
        operation.path("responses").fieldNames().asSequence().toSet()

    private fun assertErrorResponses(
        operation: JsonNode,
        vararg responseCodes: String,
    ) {
        responseCodes.forEach { responseCode ->
            val schema =
                operation
                    .path("responses")
                    .path(responseCode)
                    .path("content")
                    .path("application/json")
                    .path("schema")

            assertEquals("#/components/schemas/ErrorResponse", schema.path("\$ref").asText())
        }
    }

    private fun getGame(gameId: UUID): JsonNode {
        val response = restTemplate.getForEntity("/games/$gameId", JsonNode::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        return requireBody(response)
    }

    private fun playTurnAndAssert(
        gameId: UUID,
        humanMatchesRemoved: Int,
        computerMatchesRemoved: Int,
        expectedHeapSize: Int,
    ) {
        val turn = playTurn(gameId, humanMatchesRemoved)
        val moves = turn.path("moves")

        assertEquals(2, moves.size())
        assertMove(moves[0], player = "HUMAN", matchesRemoved = humanMatchesRemoved)
        assertMove(moves[1], player = "COMPUTER", matchesRemoved = computerMatchesRemoved)
        assertGameState(
            game = turn.path("game"),
            gameId = gameId,
            currentHeapSize = expectedHeapSize,
            currentTurn = "HUMAN",
            gameState = "GAME_ON",
            winner = null,
        )
    }

    private fun playTurn(
        gameId: UUID,
        matchesToRemove: Int,
    ): JsonNode {
        val response =
            restTemplate.postForEntity(
                "/games/$gameId/moves",
                MakeMoveRequest(matchesToRemove),
                JsonNode::class.java,
            )

        assertEquals(HttpStatus.OK, response.statusCode)
        return requireBody(response)
    }

    private fun assertGameState(
        game: JsonNode,
        gameId: UUID,
        currentHeapSize: Int,
        currentTurn: String?,
        gameState: String,
        winner: String?,
    ) {
        assertEquals(gameId.toString(), game.path("id").asText())
        assertEquals(13, game.path("initialHeapSize").asInt())
        assertEquals(currentHeapSize, game.path("currentHeapSize").asInt())
        assertNullableText(game.path("currentTurn"), currentTurn)
        assertEquals(gameState, game.path("gameState").asText())
        assertNullableText(game.path("winner"), winner)
        assertEquals("OPTIMAL", game.path("strategy").asText())
    }

    private fun assertMove(
        move: JsonNode,
        player: String,
        matchesRemoved: Int,
    ) {
        assertEquals(player, move.path("player").asText())
        assertEquals(matchesRemoved, move.path("matchesRemoved").asInt())
    }

    private fun assertNullableText(
        node: JsonNode,
        expected: String?,
    ) {
        if (expected == null) {
            assertTrue(node.isNull)
        } else {
            assertEquals(expected, node.asText())
        }
    }

    private fun requireBody(response: ResponseEntity<JsonNode>): JsonNode {
        assertTrue(response.hasBody())
        return requireNotNull(response.body)
    }
}
