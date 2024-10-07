interface MessageRepository {

    fun findById(id: Int): Message?

    fun sendMessage(senderId: Int, channelId: Int, text: String): Message

    fun getMessagesOfChannel(channelId: Int, limit: Int, skip: Int): List<Message>

}