package com.example.resizableselectionbox.logic.network

data class SearchBodyData (val source: List<String>,
    val trans_type: String,
    val request_id: String = "1",
    val detect: Boolean = true)