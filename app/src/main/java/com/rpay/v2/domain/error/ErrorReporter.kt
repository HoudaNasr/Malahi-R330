package com.rpay.v2.domain.error

interface ErrorReporter {
    fun report(error: Throwable, source: String)
    fun report(message: String, source: String)
}