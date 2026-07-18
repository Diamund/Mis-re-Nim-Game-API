# Game Rules and Planned API

> **Status:** The behavior and endpoints described here are planned and not yet implemented.

## Game rules

- A game has one heap containing a given number of matches.
- The initial number of matches must be positive.
- The human always plays first.
- Each player removes between 1 and 3 matches per turn.
- A player cannot remove more matches than remain.
- The player who removes the final match loses.

## Turn flow

1. The human submits a move.
2. The API validates and applies the move.
3. If the human removed the final match, the human loses.
4. Otherwise, the computer immediately chooses and applies its move.
5. If the computer removed the final match, the computer loses.
6. The updated game state is returned.

## Planned API

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/games` | Start a human-first game with `initialMatches` and a computer `strategy` |
| `GET` | `/games/{gameId}` | Retrieve the current game state |
| `POST` | `/games/{gameId}/moves` | Apply the human move, then automatically execute the computer move. |

### Computer strategy

- The client can select `OPTIMAL` or `RANDOM` when starting a game.
- Selection happens through the `strategy` field in `POST /games`.
- If `strategy` is omitted, it defaults to `OPTIMAL`.
- The selected strategy remains fixed for the lifetime of that game.

Each game will have a UUID and independent in-memory state. Restarting the application will discard all games.
