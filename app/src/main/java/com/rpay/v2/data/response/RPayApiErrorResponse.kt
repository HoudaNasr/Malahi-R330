package com.rpay.v2.data.response

data class RPayApiErrorResponse(
    val type: String,  // Type URI or an identifier for the error
    val title: String, // A brief description of the error
    val errors: Map<String, List<String>> // A map for validation errors (e.g., field -> list of errors)
)

