# Misère Nim API

A Kotlin and Spring Boot REST API for playing one-heap Misère Nim against the computer through Scalar, curl, or another HTTP client.

## Technology

- Kotlin 2.2.21
- Spring Boot 3.5.16
- Java 17
- Gradle 8.14.5
- JSON REST API
- OpenAPI with Scalar

## Design

GameController → GameService → GameRepository → InMemoryGameRepository

Games have UUIDs and independent in-memory state. Each game uses either the `OPTIMAL` or `RANDOM` computer strategy, selected at creation and fixed for that game. `OPTIMAL` is the default.

Game data remains available for the lifetime of the application process and is discarded when the application restarts. The project intentionally excludes a dedicated UI and persistent database.

See [Game Rules and API](docs/rules-of-the-game.md) for the game behavior and endpoint contract.

### Project structure

```text
com/wafa/miserenim/
├── MisereNimApiApplication.kt
├── GameController.kt
├── RestExceptionHandler.kt
├── domain/
│   ├── model/        # Game state and domain values
│   └── strategy/     # Optimal and random computer strategies
├── dto/              # HTTP request and response models
├── extensions/       # Domain-to-response mappings
├── repository/       # Repository contract and in-memory implementation
└── service/          # Game orchestration and application errors
```

## Build and run

Requires JDK 17. The included Gradle Wrapper provides the required Gradle version.

```bash
./gradlew build
./gradlew bootRun
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

Once started, the application is available at:

- API: `http://localhost:8080`
- Scalar: `http://localhost:8080/scalar`
- OpenAPI specification: `http://localhost:8080/v3/api-docs`

Run the tests with:

```bash
./gradlew test
```

## API usage

### Start a game

```bash
curl -X POST http://localhost:8080/games \
  -H "Content-Type: application/json" \
  -d '{"initialHeapSize":13,"strategy":"OPTIMAL"}'
```

The `strategy` field is optional and defaults to `OPTIMAL`.

Copy the UUID returned in the response:

```bash
export GAME_ID="<returned-game-id>"
```

### Make a human move

```bash
curl -X POST "http://localhost:8080/games/$GAME_ID/moves" \
  -H "Content-Type: application/json" \
  -d '{"matchesToRemove":2}'
```

If the game continues, the response contains both the human move and the automatic computer move.

### Retrieve the game state

```bash
curl "http://localhost:8080/games/$GAME_ID"
```
