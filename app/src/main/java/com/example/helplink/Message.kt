package com.example.helplink

data class Message(
    var senderId: String = "",
    var text: String = "",
    var timestamp: Long = 0
)