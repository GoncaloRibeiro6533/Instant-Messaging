package pt.isel.models

import pt.isel.Channel
import pt.isel.models.user.UserIdentifiers
import java.time.LocalDateTime

data class MessageInputModel(
    val channelId: Int,
    val content: String,
)

data class MessageOutputModel(
    val msgId: Int,
    val sender: UserIdentifiers,
    val channel: Channel,
    val content: String,
    val timestamp: LocalDateTime,
)

data class MessageInfoOutputModel(
    val msgId: Int,
    val sender: UserIdentifiers,
    val content: String,
    val timestamp: LocalDateTime,
)

data class MessageHistoryOutputModel(
    val nrOfMessages: Int,
    val channel: Channel,
    val messages: List<MessageInfoOutputModel>,
)
