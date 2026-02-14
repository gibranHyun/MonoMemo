package com.monomemo.app.domain.find

data class FindResult(
    val matches: List<IntRange>,
    val limitReached: Boolean,
)
