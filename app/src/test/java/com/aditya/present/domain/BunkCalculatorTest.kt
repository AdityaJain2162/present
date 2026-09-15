package com.aditya.present.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BunkCalculatorTest {

    @Test
    fun `above target shows safe bunks`() {
        val r = BunkCalculator.calculate(
            attendedUnits = 18,
            totalUnits = 20,
            targetPercent = 75f,
            remainingClasses = 10,
        )
        assertEquals(0.9f, r.percentage, 0.01f)
        assertTrue(r.safeBunks >= 1)
        assertEquals(0, r.recoveryNeeded)
        assertFalse(r.isBeyondSaving)
    }

    @Test
    fun `below target shows recovery needed`() {
        val r = BunkCalculator.calculate(
            attendedUnits = 5,
            totalUnits = 10,
            targetPercent = 75f,
            remainingClasses = 10,
        )
        assertEquals(0.5f, r.percentage, 0.01f)
        assertEquals(0, r.safeBunks)
        assertTrue(r.recoveryNeeded > 0)
        assertFalse(r.isBeyondSaving)
    }

    @Test
    fun `beyond saving when no remaining classes`() {
        val r = BunkCalculator.calculate(
            attendedUnits = 5,
            totalUnits = 10,
            targetPercent = 75f,
            remainingClasses = 0,
        )
        assertTrue(r.isBeyondSaving)
    }

    @Test
    fun `zero classes returns zero percentage`() {
        val r = BunkCalculator.calculate(
            attendedUnits = 0,
            totalUnits = 0,
            targetPercent = 75f,
            remainingClasses = 0,
        )
        assertEquals(0f, r.percentage, 0.01f)
        assertEquals(0, r.safeBunks)
        assertEquals(0, r.recoveryNeeded)
        assertFalse(r.isBeyondSaving)
    }
}
