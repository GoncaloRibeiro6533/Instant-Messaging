class MessageService(private val messageRepository : MessageRepository) {

    fun findMessageById(id: Int): Message? {
        return messageRepository.findMessageById(id)
    }

    fun sendMessage(channel: Channel, user: User, text: String): Message {
        return messageRepository.sendMessage(channelId, userId, text)
    }
}