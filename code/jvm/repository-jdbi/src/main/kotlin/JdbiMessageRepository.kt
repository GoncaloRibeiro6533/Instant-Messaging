import org.jdbi.v3.core.Handle

class JdbiMessageRepository(
    private val handle: Handle,
) : MessageRepository {
    override fun findById(id: Int): Message? {
        TODO("Not yet implemented")
    }

    override fun sendMessage(
        sender: User,
        channel: Channel,
        text: String,
    ): Message {
        TODO("Not yet implemented")
    }

    override fun getMsgHistory(
        channel: Channel,
        limit: Int,
        skip: Int,
    ): List<Message> {
        TODO("Not yet implemented")
    }
}
