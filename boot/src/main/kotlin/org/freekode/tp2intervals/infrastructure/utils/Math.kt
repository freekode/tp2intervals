package org.freekode.tp2intervals.infrastructure.utils

import kotlin.math.absoluteValue

class Math {
    companion object {
        fun percentageDiff(first: Double, second: Double): Double = (first - second).absoluteValue * 100 / second
    }
}
