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

        val safeBunks = classesCanBunk(attendedUnits, totalUnits, target)
        val recoveryNeeded = classesToAttend(attendedUnits, totalUnits, target)

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

    fun classesCanBunk(attended: Int, total: Int, target: Float): Int {
        if (target >= 1f || total <= 0) return 0
        val maxAllowed = (attended.toFloat() / target).toInt() - total
        return maxOf(0, maxAllowed)
    }

    fun classesToAttend(attended: Int, total: Int, target: Float): Int {
        if (target <= 0f || total <= 0) return 0
        if (attended.toFloat() / total >= target) return 0
        val need = Math.ceil(((target * total - attended).toFloat() / (1f - target)).toDouble()).toInt()
        return maxOf(0, need)
    }
}
