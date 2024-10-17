import org.jdbi.v3.core.Handle

class JdbiMessageRepository(
    private val handle: Handle,
) : MessageRepository {
    override fun findById(id: Int): Message? =
        handle.createQuery(
            """
        SELECT * FROM message WHERE id = :id
        """,
        ).bind("id", id)
            .mapTo(Message::class.java)
            .findFirst()
            .orElse(
                null,
            )

    override fun createMessage(
        sender: User,
        channel: Channel,
        text: String,
    ): Message =
        handle.createUpdate(
            """
        INSERT INTO message(creationtime, user_id, channel_id, message) values 
        (now(), :userId, :channelId, :message)
        """,
        ).bind("user", sender.id)
            .bind("channel", channel.id)
            .bind("text", text)
            .executeAndReturnGeneratedKeys()
            .mapTo(Message::class.java)
            .one()

    override fun findByChannel(
        channel: Channel,
        limit: Int,
        skip: Int,
    ): List<Message> {
        return handle.createQuery(
            """
            SELECT * FROM message WHERE channel_id = :channelId ORDER BY creationtime DESC
            LIMIT :limit OFFSET :skip
            """,
        ).bind("channelId", channel.id)
            .bind("limit", limit)
            .bind("skip", skip)
            .mapTo(Message::class.java)
            .list()
    }

    override fun deleteMessageById(id: Int): Message =
        handle.createUpdate(
            """
            DELETE FROM message WHERE id = :id
            """,
        ).bind("id", id)
            .executeAndReturnGeneratedKeys()
            .mapTo(Message::class.java)
            .one()

    override fun deleteMessagesByChannel(channelId: Int): List<Message> =
        handle.createUpdate(
            """
            DELETE FROM dbo.message WHERE channel_id = :channelId
            """,
        ).bind("channelId", channelId)
            .executeAndReturnGeneratedKeys()
            .mapTo(Message::class.java)
            .list()

    override fun findAll(): List<Message> =
        handle.createQuery(
            """
            SELECT * FROM dbo.message
            """,
        ).mapTo(Message::class.java)
            .list()

    override fun clear() {
        handle.createUpdate(
            """
            DELETE FROM dbo.message
            """,
        ).execute()
    }
}
