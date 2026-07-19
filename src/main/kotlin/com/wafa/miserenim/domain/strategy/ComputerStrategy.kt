package com.wafa.miserenim.domain.strategy

interface ComputerStrategy {
    fun chooseMove(currentHeapSize: Int): Int
}
