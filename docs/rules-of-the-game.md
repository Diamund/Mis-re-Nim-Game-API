# Game Rules and Planned API

> **Status:** Planned and not yet implemented.

## Game rules

- A game starts with a positive `initialHeapSize` of matchsticks.
- Each player removes between 1 and 3 matchsticks without exceeding the number currently in the heap.
- The player who removes the final matchstick loses.

## Turn flow

The human and computer alternate turns, with the human playing first. After a valid human move, the computer automatically takes its turn. This continues until no matchsticks remain. The API returns the performed moves and updated game state.

## Planned API

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/games` | Start a game with `initialHeapSize` and an optional `strategy` |
| `GET` | `/games/{gameId}` | Retrieve the current game state |
| `POST` | `/games/{gameId}/moves` | Apply a human move and automatically execute the computer response |

## Game state

- `initialHeapSize`: original heap size; never changes.
- `currentHeapSize`: current number of matchsticks in the heap.
- `currentTurn`: `HUMAN` or `COMPUTER`; `null` when the game is over.
- `gameState`: `GAME_ON` or `GAME_OVER`.
- `winner`: `null` while `gameState` is `GAME_ON`; `HUMAN` or `COMPUTER` when `gameState` is `GAME_OVER`.

A new game starts with equal initial and current heap sizes, `currentTurn` set to `HUMAN`, and `gameState` set to `GAME_ON`.

## Computer strategy

The client selects `OPTIMAL` or `RANDOM` when creating a game. The strategy defaults to `OPTIMAL` and remains fixed throughout the game.

Each game has a UUID and independent in-memory state. Restarting the application discards all games.