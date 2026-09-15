package com.aditya.present.util

object AcronymGenerator {
    private val fillerWords = setOf("and", "of", "the", "for", "in", "on", "at", "to", "a", "an")

    fun generate(name: String): String {
        val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return ""
        if (words.size == 1) return words[0].take(3).uppercase()

        val filtered = words.filter { it.lowercase() !in fillerWords }
        val source = if (filtered.size >= 2) filtered else words
        return source.take(3).joinToString("") { it.first().uppercaseChar().toString() }
    }
}
