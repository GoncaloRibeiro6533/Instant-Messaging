class MessageService(private val messageRepository : MessageRepository) {

    fun findMessageById(id: Int): Message? {
        return messageRepository.findMessageById(id)
    }

    fun sendMessage(channelId: Int, userId: Int, text: String): Message {
        return messageRepository.sendMessage(channelId, userId, text)
    }
}