# Misère Nim API

A Kotlin & Spring Boot REST API project for playing one-heap Misère Nim against the computer through an HTTP client.

> **Status:** The core game domain, computer move strategies, and in-memory repository are implemented. Services, REST endpoints, and their tests are planned but not yet implemented.

## Technology

* Kotlin 2.2.21
* Spring Boot 3.5.16
* Java 17
* Gradle 8.14.5
* JSON REST API
* OpenAPI with Scalar

## Implementation

See [Game Rules and Planned API](docs/rules-of-the-game.md) for the intended game behavior and endpoint contract.

Layers: GameController → GameService → GameRepository → InMemoryGameRepository

Games use UUIDs and independent in-memory state. The repository stores them for the lifetime of the application process.
Each game uses either the `OPTIMAL` or `RANDOM` strategy, selected at creation and fixed for that game. `OPTIMAL` is the default.

The scope intentionally excludes a UI and persistent database.

### Project structure

com/wafa/miserenim/

├── MisereNimApiApplication.kt
├── GameController.kt                # later
├── domain/
│   ├── model/
│   │   ├── Game.kt
│   │   ├── Move.kt
│   │   ├── Player.kt
│   │   ├── GameState.kt
│   │   └── StrategyType.kt
│   └── strategy/
│       ├── ComputerStrategy.kt
│       ├── OptimalComputerStrategy.kt
│       └── RandomComputerStrategy.kt
├── dto/                             # later
├── repository/
│   ├── GameRepository.kt
│   └── InMemoryGameRepository.kt
└── service/                         # later

## Build and run

Requires JDK 17.

```bash
./gradlew build
./gradlew bootRun
```

The application runs at `http://localhost:8080`.

Scalar is available at `http://localhost:8080/scalar`. Game operations will appear after the endpoints are implemented.

Run the tests with:

```bash
./gradlew test
```

## Planned curl examples

These commands document the intended API and will not work until the endpoints are implemented.

Start a game:

```bash
curl -X POST http://localhost:8080/games \
  -H "Content-Type: application/json" \
  -d '{"initialHeapSize":13,"strategy":"OPTIMAL"}'
```

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
