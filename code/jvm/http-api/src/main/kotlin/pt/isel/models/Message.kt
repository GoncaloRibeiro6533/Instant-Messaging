package pt.isel.models

import pt.isel.models.channel.ChannelIdentifiers
import pt.isel.models.channel.ChannelOutputModel
import pt.isel.models.user.UserIdentifiers
import java.time.LocalDateTime

data class MessageInputModel(
    val channelId: Int,
    val content: String,
)

data class MessageOutputModel(
    val msgId: Int,
    val sender: UserIdentifiers,
    val channel: ChannelIdentifiers,
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
    val channelId: Int,
    val channelName: String,
    val nrOfMessages: Int,
    val channel: ChannelIdentifiers,
    val messages: List<MessageInfoOutputModel>,
)
