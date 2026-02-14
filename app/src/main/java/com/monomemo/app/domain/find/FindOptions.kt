package com.monomemo.app.domain.find

data class FindOptions(
    val caseSensitive: Boolean = false,
    val wholeWord: Boolean = false,
)
