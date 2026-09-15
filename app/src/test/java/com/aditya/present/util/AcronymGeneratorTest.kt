package com.aditya.present.util

import org.junit.Assert.assertEquals
import org.junit.Test

class AcronymGeneratorTest {

    @Test
    fun `single word takes first 3 letters`() {
        assertEquals("MAT", AcronymGenerator.generate("Mathematics"))
    }

    @Test
    fun `multiple words takes first letter of each skipping fillers`() {
        assertEquals("DCN", AcronymGenerator.generate("Data Communication and Networks"))
    }

    @Test
    fun `all filler words falls back to first letters`() {
        assertEquals("DOT", AcronymGenerator.generate("Data of the"))
    }

    @Test
    fun `empty string returns empty`() {
        assertEquals("", AcronymGenerator.generate(""))
    }
}
