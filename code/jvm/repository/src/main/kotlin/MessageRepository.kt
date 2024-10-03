interface MessageRepository {

    fun findMessageById(id: Int): Message?

    fun sendMessage(channelId: Int, userId: Int, text: String): Message

}