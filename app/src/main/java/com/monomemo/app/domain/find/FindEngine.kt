package com.monomemo.app.domain.find

object FindEngine {

    private const val SOFT_LIMIT = 1000

    fun findMatches(
        text: String,
        query: String,
        options: FindOptions = FindOptions(),
    ): FindResult {
        if (query.isEmpty()) return FindResult(emptyList(), false)

        val searchText = if (options.caseSensitive) text else text.lowercase()
        val searchQuery = if (options.caseSensitive) query else query.lowercase()

        val matches = mutableListOf<IntRange>()
        var startIndex = 0

        while (startIndex <= searchText.length - searchQuery.length) {
            val index = searchText.indexOf(searchQuery, startIndex)
            if (index == -1) break

            val end = index + searchQuery.length

            if (options.wholeWord) {
                val beforeOk = index == 0 || !isWordChar(text[index - 1])
                val afterOk = end == text.length || !isWordChar(text[end])
                if (beforeOk && afterOk) {
                    matches.add(index until end)
                    if (matches.size >= SOFT_LIMIT) {
                        return FindResult(matches, true)
                    }
                }
            } else {
                matches.add(index until end)
                if (matches.size >= SOFT_LIMIT) {
                    return FindResult(matches, true)
                }
            }

            startIndex = index + 1
        }

        return FindResult(matches, false)
    }

    private fun isWordChar(c: Char): Boolean {
        return c.isLetterOrDigit() || c == '_'
    }
}
