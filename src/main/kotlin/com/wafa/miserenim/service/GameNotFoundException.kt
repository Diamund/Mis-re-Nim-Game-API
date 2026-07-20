package com.wafa.miserenim.service

import java.util.UUID

class GameNotFoundException(
    val gameId: UUID,
) : RuntimeException("Game not found: $gameId")
