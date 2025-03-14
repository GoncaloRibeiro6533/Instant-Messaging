package pt.isel

import jakarta.inject.Named
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    data object MessageTooLong : MessageError()
}

@Named
class MessageService(
    private val trxManager: TransactionManager,
    private val emitter: UpdatesEmitter,
) {
    fun findMessageById(
        id: Int,
        userId: Int,
    ): Either<MessageError, Message> =
        trxManager.run {
            val user = userRepo.findById(userId) ?: return@run failure(MessageError.UserNotFound)
            if (id < 0) return@run failure(MessageError.NegativeIdentifier)
            val msg = messageRepo.findById(id) ?: return@run failure(MessageError.MessageNotFound)
            if (!channelRepo.getChannelMembers(msg.channel).containsKey(user)) return@run failure(MessageError.UserNotInChannel)
            return@run success(msg)
        }

    fun sendMessage(
        channelId: Int,
        userId: Int,
        text: String,
    ): Either<MessageError, Message> =
        trxManager.run {
            val user = userRepo.findById(userId) ?: return@run failure(MessageError.UserNotFound)
            if (text.length > Message.MAX_MESSAGE_LENGTH) return@run failure(MessageError.MessageTooLong)
            if (channelId < 0) return@run failure(MessageError.InvalidChannelId)
            if (text.isBlank()) return@run failure(MessageError.InvalidText)
            if (userId < 0) return@run failure(MessageError.InvalidUserId)
            val channel = channelRepo.findById(channelId) ?: return@run failure(MessageError.ChannelNotFound)
            val members = channelRepo.getChannelMembers(channel)
            if (!members.containsKey(user)) return@run failure(MessageError.UserNotInChannel)
            val message = messageRepo.createMessage(user, channel, text, LocalDateTime.now())
            CoroutineScope(Dispatchers.IO).launch {
                emitter.sendEventOfNewMessage(message, members.keys)
            }
            return@run success(message)
        }

    fun getMsgHistory(
        channelId: Int,
        limit: Int = 10,
        skip: Int = 0,
        userId: Int,
    ): Either<MessageError, Pair<Channel, List<Message>>> =
        trxManager.run {
            val user = userRepo.findById(userId) ?: return@run failure(MessageError.UserNotFound)
            if (channelId < 0) return@run failure(MessageError.InvalidChannelId)
            if (limit < 0) return@run failure(MessageError.InvalidLimit)
            if (skip < 0) return@run failure(MessageError.InvalidSkip)
            val channel = channelRepo.findById(channelId) ?: return@run failure(MessageError.ChannelNotFound)
            if (!channelRepo.getChannelMembers(channel).containsKey(user)) return@run failure(MessageError.UserNotInChannel)
            val messages = messageRepo.findByChannel(channel, limit, skip)
            return@run success(channel to messages)
        }
}
