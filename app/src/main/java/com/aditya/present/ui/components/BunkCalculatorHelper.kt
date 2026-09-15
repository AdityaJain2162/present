package com.aditya.present.ui.components

import kotlin.math.ceil
import kotlin.math.max

object BunkCalculatorHelper {
    fun classesCanBunk(attended: Int, total: Int, target: Float): Int {
        if (target >= 1f) return 0
        // attended / (total + skip) >= target
        // attended >= target * (total + skip)
        // skip <= (attended / target) - total
        val maxAllowed = (attended / target).toInt() - total
        return max(0, maxAllowed)
    }

    fun classesToAttend(attended: Int, total: Int, target: Float): Int {
        if (target <= 0f) return 0
        // (attended + need) / (total + need) >= target
        // attended + need >= target * (total + need)
        // attended + need >= target * total + target * need
        // need * (1 - target) >= target * total - attended
        // need >= (target * total - attended) / (1 - target)
        if (attended.toFloat() / total >= target) return 0
        val need = ceil((target * total - attended) / (1f - target)).toInt()
        return max(0, need)
    }
}
