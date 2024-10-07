class MessageService(private val messageRepository : MessageRepository) {

    fun findMessageById(id: Int): Message? {
        return messageRepository.findById(id)
    }

    fun sendMessage(channelId: Int, userId: Int, text: String): Message {
        return messageRepository.sendMessage(channelId, userId, text)
    }

    fun getMessagesOfChannel(channelId: Int, limit: Int, skip: Int): List<Message> {
        return messageRepository.getMessagesOfChannel(channelId, limit, skip)
    }
/*
    fun getMsgHistory(channelId: Int, limit: Int = 5, skip: Int=5) : Either<ChannelError, List<Message>> = trxManager.run {
        if (channelId < 0) return@run Either.Left(ChannelError.NegativeIdentifier)
        //todo maybe user must be authenticated to see messages?
        return@run Either.Right(channelRepo.getMsgHistory(channelId, limit, skip))
    }

 */
}