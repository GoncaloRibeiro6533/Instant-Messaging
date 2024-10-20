package pt.isel.models

import java.time.LocalDateTime

data class MessageInputModel(
    val channelId: Int,
    val content: String,
)

data class MessageOutputModel(
    val msgId: Int,
    val senderId: Int,
    val senderName: String,
    val channelId: Int,
    val channelName: String,
    val content: String,
    val timestamp: LocalDateTime,
)

data class MessageInfoOutputModel(
    val msgId: Int,
    val senderId: Int,
    val senderName: String,
    val content: String,
    val timestamp: LocalDateTime,
)

data class MessageHistoryOutputModel(
    val channelId: Int,
    val channelName: String,
    val messages: List<MessageInfoOutputModel>,
)
