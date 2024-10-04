interface MessageRepository {

    fun findMessageById(id: Int): Message?

    fun sendMessage(senderId: Int, channelId: Int, text: String): Message

}