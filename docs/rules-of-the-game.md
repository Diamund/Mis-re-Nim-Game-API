# Game Rules and API

> **Status:** The core game domain, computer move strategies, in-memory repository, service orchestration, and REST API are implemented.

## Game rules

- A game starts with a positive `initialHeapSize` of matchsticks.
- Each player removes between 1 and 3 matchsticks without exceeding the number currently in the heap.
- The player who removes the final matchstick loses.

## Turn flow

The human and computer alternate turns, with the human playing first. After a valid human move, the computer automatically takes its turn if the human move does not end the game. If the human removes the final matchstick, the response contains only the human move. This continues until no matchsticks remain. The API returns the performed moves and updated game state.

## API

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/games` | Start a game with `initialHeapSize` and an optional `strategy` |
| `GET` | `/games/{gameId}` | Retrieve the current game state |
| `POST` | `/games/{gameId}/moves` | Apply a human move and automatically execute the computer response if the game continues |

## Game state

- `id`: UUID identifying the game.
- `initialHeapSize`: original heap size; never changes.
- `currentHeapSize`: current number of matchsticks in the heap.
- `currentTurn`: `HUMAN` or `COMPUTER`; `null` when the game is over.
- `gameState`: `GAME_ON` or `GAME_OVER`.
- `winner`: `null` while `gameState` is `GAME_ON`; `HUMAN` or `COMPUTER` when `gameState` is `GAME_OVER`.
- `strategy`: `OPTIMAL` or `RANDOM`; selected when the game is created and never changes.

A new game starts with equal initial and current heap sizes, `currentTurn` set to `HUMAN`, and `gameState` set to `GAME_ON`.

## Computer strategy

The computer move selectors support `OPTIMAL` and `RANDOM` strategies:

- `OPTIMAL` leaves a heap of size `1 mod 4` whenever possible. If no winning move exists, it removes one matchstick.
- `RANDOM` chooses uniformly from all legal moves, including a move that removes the final matchstick.

The API accepts a strategy when creating a game. The strategy defaults to `OPTIMAL` and remains fixed throughout the game.

The in-memory repository stores each game by UUID and keeps game state independent. Restarting the application discards all games. The service uses this storage to coordinate complete human/computer turns exposed through the REST API.
