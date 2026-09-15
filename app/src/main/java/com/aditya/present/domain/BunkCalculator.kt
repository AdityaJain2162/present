package com.aditya.present.domain

object BunkCalculator {

    data class Result(
        val attendedUnits: Int,
        val totalUnits: Int,
        val percentage: Float,
        val safeBunks: Int,
        val recoveryNeeded: Int,
        val isBeyondSaving: Boolean,
    )

    fun calculate(
        attendedUnits: Int,
        totalUnits: Int,
        targetPercent: Float,
        remainingClasses: Int,
    ): Result {
        val target = targetPercent / 100f
        val percentage = if (totalUnits > 0) attendedUnits.toFloat() / totalUnits else 0f

        val safeBunks = if (totalUnits > 0 && attendedUnits.toFloat() / target > totalUnits) {
            ((attendedUnits.toFloat() / target) - totalUnits).toInt()
        } else 0

        val recoveryNeeded = if (percentage < target && totalUnits > 0) {
            val needed = ((target * totalUnits - attendedUnits) / (1f - target))
            Math.ceil(needed.toDouble()).toInt().coerceAtLeast(0)
        } else 0

        val isBeyondSaving = totalUnits > 0 && percentage < target &&
            (attendedUnits + remainingClasses).toFloat() / (totalUnits + remainingClasses) < target

        return Result(
            attendedUnits = attendedUnits,
            totalUnits = totalUnits,
            percentage = percentage,
            safeBunks = safeBunks,
            recoveryNeeded = recoveryNeeded,
            isBeyondSaving = isBeyondSaving,
        )
    }
}
