import org.jdbi.v3.core.Handle

class JdbiMessageRepository (
    private val handle: Handle
) :MessageRepository {

    override fun findById(id: Int): Message? = handle.createQuery(
        """
        SELECT * FROM message
        WHERE id = :id
        """
    ).bind("id", id)
        .mapTo(Message::class.java)
        .findFirst()
        .orElse(null
        )

    override fun sendMessage(sender: User, channel: Channel, text: String): Message = handle.createUpdate(
        """
        INSERT INTO message(id, creationtime, user_id, channel_id, message) values 
        (nextval('message_id_seq'), now(), :userId, :channelId, :message)
        """
    ).bind("user", sender.id)
        .bind("channel", channel.id)
        .bind("text", text)
        .executeAndReturnGeneratedKeys()
        .mapTo(Message::class.java)
        .one()


    override fun getMsgHistory(channel: Channel, limit: Int, skip: Int): List<Message> {
        return handle.createQuery(
            """
            SELECT * FROM message
            WHERE channel_id = :channelId
            ORDER BY creationtime DESC
            LIMIT :limit OFFSET :skip
            """
        ).bind("channelId", channel.id)
            .bind("limit", limit)
            .bind("skip", skip)
            .mapTo(Message::class.java)
            .list()
    }

}




