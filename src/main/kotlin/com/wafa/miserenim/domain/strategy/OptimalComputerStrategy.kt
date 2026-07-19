package com.wafa.miserenim.domain.strategy

class OptimalComputerStrategy : ComputerStrategy {

    override fun chooseMove(currentHeapSize: Int): Int {
        require(currentHeapSize > 0) { "Current heap size must be positive" }

        val winningMove = (currentHeapSize - 1) % 4
        return if (winningMove == 0) 1 else winningMove
    }
}
