package com.wafa.miserenim.domain.strategy

import kotlin.random.Random

class RandomComputerStrategy(
    private val random: Random = Random.Default,
) : ComputerStrategy {

    override fun chooseMove(currentHeapSize: Int): Int {
        require(currentHeapSize > 0) { "Current heap size must be positive" }

        val maximumMatchesToRemove = minOf(3, currentHeapSize)
        return random.nextInt(from = 1, until = maximumMatchesToRemove + 1)
    }
}
