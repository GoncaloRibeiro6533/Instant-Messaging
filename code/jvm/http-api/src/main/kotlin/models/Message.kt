package models

import java.time.LocalDateTime

data class MessageInputModel(
    val channelId: Int,
    val userId: Int,
    val content: String,
)

data class MessageOutputModel(
    val id: Int,
    val channelId: Int,
    val userId: Int,
    val content: String,
    val timestamp: LocalDateTime,
)
