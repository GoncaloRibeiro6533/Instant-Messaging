

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
            val user = userRepo.findByToken(token) ?: return@run Either.Left(MessageError.Unauthorized)
            if (id < 0) return@run Either.Left(MessageError.NegativeIdentifier)

            val msg = messageRepo.findById(id) ?: return@run Either.Left(MessageError.MessageNotFound)

            if (!channelRepo.getChannelMembers(msg.channel).contains(user.id)) return@run Either.Left(MessageError.UserNotInChannel)

            return@run Either.Right(msg)
        }

    fun sendMessage(
        channelId: Int,
        userId: Int,
        text: String,
        token: String,
    ): Any =
        trxManager.run {
            val autenticatedUser = userRepo.findByToken(token) ?: return@run Either.Left(MessageError.Unauthorized)
            if (channelId < 0) return@run Either.Left(MessageError.InvalidChannelId)
            if (text.isBlank()) return@run Either.Left(MessageError.InvalidText)
            if (userId < 0) return@run Either.Left(MessageError.InvalidUserId)
            if (autenticatedUser.id != userId) return@run Either.Left(MessageError.Unauthorized)
            val channel = channelRepo.findById(channelId) ?: return@run Either.Left(MessageError.ChannelNotFound)
            val members = channelRepo.getChannelMembers(channel)
            if (!members.contains(userId)) return@run Either.Left(MessageError.UserNotInChannel)
            return@run Either.Right(messageRepo.sendMessage(autenticatedUser, channel, text))
        }

    fun getMsgHistory(
        channelId: Int,
        limit: Int,
        skip: Int,
        token: String,
    ): Either<MessageError, List<Message>> =
        trxManager.run {
            val user = userRepo.findByToken(token) ?: return@run Either.Left(MessageError.Unauthorized)

            if (channelId < 0) return@run Either.Left(MessageError.InvalidChannelId)
            if (limit < 0) return@run Either.Left(MessageError.InvalidLimit)
            if (skip < 0) return@run Either.Left(MessageError.InvalidSkip)

            val channel = channelRepo.findById(channelId) ?: return@run Either.Left(MessageError.ChannelNotFound)

            if (!channelRepo.getChannelMembers(channel).contains(user.id)) return@run Either.Left(MessageError.UserNotInChannel)

            return@run Either.Right(messageRepo.getMsgHistory(channel, limit, skip))
        }
}
