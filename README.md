# Misère Nim API

A Kotlin & Spring Boot REST API project for playing one-heap Misère Nim against the computer through an HTTP client.

> **Status:** The core game domain, computer move strategies, in-memory repository, service orchestration, and REST API are implemented.

## Technology

* Kotlin 2.2.21
* Spring Boot 3.5.16
* Java 17
* Gradle 8.14.5
* JSON REST API
* OpenAPI with Scalar

## Implementation

See [Game Rules and API](docs/rules-of-the-game.md) for the game behavior and endpoint contract.

Layers: GameController → GameService → GameRepository → InMemoryGameRepository

Games use UUIDs and independent in-memory state. The repository stores them for the lifetime of the application process.
Each game uses either the `OPTIMAL` or `RANDOM` strategy, selected at creation and fixed for that game. `OPTIMAL` is the default.

The scope intentionally excludes a UI and persistent database.

### Project structure

com/wafa/miserenim/

├── MisereNimApiApplication.kt
├── GameController.kt
├── RestExceptionHandler.kt
├── domain/
│   ├── model/
│   │   ├── Game.kt
│   │   ├── Move.kt
│   │   ├── Player.kt
│   │   ├── GameState.kt
│   │   ├── StrategyType.kt
│   │   └── TurnResult.kt
│   └── strategy/
│       ├── ComputerStrategy.kt
│       ├── OptimalComputerStrategy.kt
│       └── RandomComputerStrategy.kt
├── dto/
│   ├── CreateGameRequest.kt
│   ├── ErrorResponse.kt
│   ├── GameResponse.kt
│   ├── MakeMoveRequest.kt
│   ├── MoveResponse.kt
│   └── PlayTurnResponse.kt
├── extensions/
│   └── ResponseMappings.kt
├── repository/
│   ├── GameRepository.kt
│   └── InMemoryGameRepository.kt
└── service/
    ├── GameNotFoundException.kt
    └── GameService.kt

## Build and run

Requires JDK 17.

```bash
./gradlew build
./gradlew bootRun
```

The application runs at `http://localhost:8080`.

Scalar is available at `http://localhost:8080/scalar`, including the game operations.

Run the tests with:

```bash
./gradlew test
```

## Curl examples

Start a game:

```bash
curl -X POST http://localhost:8080/games \
  -H "Content-Type: application/json" \
  -d '{"initialHeapSize":13,"strategy":"OPTIMAL"}'
```

The `strategy` field is optional and defaults to `OPTIMAL`.

Use the UUID returned by that request:

```bash
export GAME_ID="<returned-game-id>"
```

Make a human move:

```bash
curl -X POST "http://localhost:8080/games/$GAME_ID/moves" \
  -H "Content-Type: application/json" \
  -d '{"matchesToRemove":2}'
```

Retrieve the game state:

```bash
curl "http://localhost:8080/games/$GAME_ID"
```
