

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

class MessageService(private val trxManager: TransactionManager) {
    fun findMessageById(
        id: Int,
        token: String,
    ): Either<MessageError, Message?> =
        trxManager.run {
            val user = userRepo.findByToken(token) ?: return@run failure(MessageError.Unauthorized)
            if (id < 0) return@run failure(MessageError.NegativeIdentifier)

            val msg = messageRepo.findById(id) ?: return@run failure(MessageError.MessageNotFound)

            if (!channelRepo.getChannelMembers(msg.channel).contains(user.id)) return@run failure(MessageError.UserNotInChannel)

            return@run success(msg)
        }

    fun sendMessage(
        channelId: Int,
        userId: Int,
        text: String,
        token: String,
    ): Any =
        trxManager.run {
            val autenticatedUser = userRepo.findByToken(token) ?: return@run failure(MessageError.Unauthorized)
            if (channelId < 0) return@run failure(MessageError.InvalidChannelId)
            if (text.isBlank()) return@run failure(MessageError.InvalidText)
            if (userId < 0) return@run failure(MessageError.InvalidUserId)
            if (autenticatedUser.id != userId) return@run failure(MessageError.Unauthorized)
            val channel = channelRepo.findById(channelId) ?: return@run failure(MessageError.ChannelNotFound)
            val members = channelRepo.getChannelMembers(channel)
            if (!members.contains(userId)) return@run failure(MessageError.UserNotInChannel)
            return@run success(messageRepo.sendMessage(autenticatedUser, channel, text))
        }

    fun getMsgHistory(
        channelId: Int,
        limit: Int,
        skip: Int,
        token: String,
    ): Either<MessageError, List<Message>> =
        trxManager.run {
            val user = userRepo.findByToken(token) ?: return@run failure(MessageError.Unauthorized)

            if (channelId < 0) return@run failure(MessageError.InvalidChannelId)
            if (limit < 0) return@run failure(MessageError.InvalidLimit)
            if (skip < 0) return@run failure(MessageError.InvalidSkip)

            val channel = channelRepo.findById(channelId) ?: return@run failure(MessageError.ChannelNotFound)

            if (!channelRepo.getChannelMembers(channel).contains(user.id)) return@run failure(MessageError.UserNotInChannel)

            return@run success(messageRepo.getMsgHistory(channel, limit, skip))
        }
}
