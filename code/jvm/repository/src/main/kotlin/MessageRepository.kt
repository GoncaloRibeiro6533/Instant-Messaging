interface MessageRepository {

    fun findById(id: Int): Message?

    fun sendMessage(senderId: Int, channelId: Int, text: String): Message

    fun getMsgHistory(channelId: Int, limit: Int, skip: Int): List<Message>

    //fun getMsgHistory(channelId: Int, limit: Int, skip: Int): List<Message>

}