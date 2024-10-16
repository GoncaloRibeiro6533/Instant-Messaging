import jakarta.inject.Named

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

    data object SessionExpired : MessageError()
}

@Named
class MessageService(private val trxManager: TransactionManager) {
    fun findMessageById(
        id: Int,
        token: String,
    ): Either<MessageError, Message?> =
        trxManager.run {
            val session = sessionRepo.findByToken(token) ?: return@run failure(MessageError.Unauthorized)
            if (session.expired()) {
                sessionRepo.deleteSession(token)
                return@run failure(MessageError.SessionExpired)
            }
            if (id < 0) return@run failure(MessageError.NegativeIdentifier)
            val msg = messageRepo.findById(id) ?: return@run failure(MessageError.MessageNotFound)
            if (!channelRepo.getChannelMembers(msg.channel).contains(session.userId)) return@run failure(MessageError.UserNotInChannel)
            return@run success(msg)
        }

    fun sendMessage(
        channelId: Int,
        userId: Int,
        text: String,
        token: String,
    ): Any =
        trxManager.run {
            val session = sessionRepo.findByToken(token) ?: return@run failure(MessageError.Unauthorized)
            if (session.expired()) {
                sessionRepo.deleteSession(token)
                return@run failure(MessageError.SessionExpired)
            }
            val user = userRepo.findById(userId) ?: return@run failure(MessageError.UserNotFound)
            if (channelId < 0) return@run failure(MessageError.InvalidChannelId)
            if (text.isBlank()) return@run failure(MessageError.InvalidText)
            if (userId < 0) return@run failure(MessageError.InvalidUserId)
            if (session.userId != userId) return@run failure(MessageError.Unauthorized)
            val channel = channelRepo.findById(channelId) ?: return@run failure(MessageError.ChannelNotFound)
            val members = channelRepo.getChannelMembers(channel)
            if (!members.contains(userId)) return@run failure(MessageError.UserNotInChannel)
            return@run success(messageRepo.createMessage(user, channel, text))
        }

    fun getMsgHistory(
        channelId: Int,
        limit: Int,
        skip: Int,
        token: String,
    ): Either<MessageError, List<Message>> =
        trxManager.run {
            val session = sessionRepo.findByToken(token) ?: return@run failure(MessageError.Unauthorized)
            if (session.expired()) {
                sessionRepo.deleteSession(token)
                return@run failure(MessageError.SessionExpired)
            }
            if (channelId < 0) return@run failure(MessageError.InvalidChannelId)
            if (limit < 0) return@run failure(MessageError.InvalidLimit)
            if (skip < 0) return@run failure(MessageError.InvalidSkip)
            val channel = channelRepo.findById(channelId) ?: return@run failure(MessageError.ChannelNotFound)
            if (!channelRepo.getChannelMembers(channel).contains(session.userId)) return@run failure(MessageError.UserNotInChannel)
            return@run success(messageRepo.findByChannel(channel, limit, skip))
        }
}
