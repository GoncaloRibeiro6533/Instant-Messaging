package pt.isel

import jakarta.inject.Named
import java.time.LocalDateTime

sealed class MessageError {
    data object MessageNotFound : MessageError()

    data object InvalidChannelId : MessageError()

    data object InvalidText : MessageError()

    data object InvalidLimit : MessageError()

    data object InvalidSkip : MessageError()

    data object NegativeIdentifier : MessageError()

    data object Unauthorized : MessageError()

    data object UserNotFound : MessageError()

    data object InvalidUserId : MessageError()

    data object ChannelNotFound : MessageError()

    data object UserNotInChannel : MessageError()
}

@Named
class MessageService(private val trxManager: TransactionManager) {
    fun findMessageById(
        id: Int,
        userId: Int,
    ): Either<MessageError, Message> =
        trxManager.run {
            userRepo.findById(userId) ?: return@run failure(MessageError.UserNotFound)
            if (id < 0) return@run failure(MessageError.NegativeIdentifier)
            val msg = messageRepo.findById(id) ?: return@run failure(MessageError.MessageNotFound)
            if (!channelRepo.getChannelMembers(msg.channel).contains(userId)) return@run failure(MessageError.UserNotInChannel)
            return@run success(msg)
        }

    fun sendMessage(
        channelId: Int,
        userId: Int,
        text: String,
    ): Either<MessageError, Message> =
        trxManager.run {
            val user = userRepo.findById(userId) ?: return@run failure(MessageError.UserNotFound)
            if (channelId < 0) return@run failure(MessageError.InvalidChannelId)
            if (text.isBlank()) return@run failure(MessageError.InvalidText)
            if (userId < 0) return@run failure(MessageError.InvalidUserId)
            val channel = channelRepo.findById(channelId) ?: return@run failure(MessageError.ChannelNotFound)
            val members = channelRepo.getChannelMembers(channel)
            if (!members.contains(userId)) return@run failure(MessageError.UserNotInChannel)
            return@run success(messageRepo.createMessage(user, channel, text, LocalDateTime.now()))
        }

    fun getMsgHistory(
        channelId: Int,
        limit: Int,
        skip: Int,
        userId: Int,
    ): Either<MessageError, List<Message>> =
        trxManager.run {
            if (channelId < 0) return@run failure(MessageError.InvalidChannelId)
            if (limit < 0) return@run failure(MessageError.InvalidLimit)
            if (skip < 0) return@run failure(MessageError.InvalidSkip)
            val channel = channelRepo.findById(channelId) ?: return@run failure(MessageError.ChannelNotFound)
            if (!channelRepo.getChannelMembers(channel).contains(userId)) return@run failure(MessageError.UserNotInChannel)
            return@run success(messageRepo.findByChannel(channel, limit, skip))
        }
}
