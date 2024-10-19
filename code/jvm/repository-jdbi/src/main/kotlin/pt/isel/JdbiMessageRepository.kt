package pt.isel

import org.jdbi.v3.core.Handle
import java.sql.ResultSet

class JdbiMessageRepository(
    private val handle: Handle,
) : MessageRepository {
    override fun findById(id: Int): Message? =
        handle.createQuery(
            """
        SELECT m.*, u.username, u.email, c.name, c.visibility 
        FROM dbo.MESSAGE m JOIN dbo.USER u ON m.user_id = u.id JOIN dbo.channel c ON m.channel_id = c.id
        WHERE m.id = :id;
        """,
        ).bind("id", id)
            .map { rs, _ -> mapRowToMessage(rs) }
            .findFirst()
            .orElse(
                null,
            )

    override fun createMessage(
        sender: User,
        channel: Channel,
        text: String,
    ): Message {
        val id =
            handle.createUpdate(
                """
                INSERT INTO dbo.message(creationtime, user_id, channel_id, message) values 
                (now(), :userId, :channelId, :message)
                """,
            ).bind("user", sender.id)
                .bind("channel", channel.id)
                .bind("text", text)
                .executeAndReturnGeneratedKeys().mapTo(Int::class.java).one()
        return Message(
            id,
            sender,
            channel,
            text,
            TODO(),
        )
    }

    override fun findByChannel(
        channel: Channel,
        limit: Int,
        skip: Int,
    ): List<Message> {
        return handle.createQuery(
            """
            SELECT m.*, u.username, u.email, c.name, c.visibility 
            FROM dbo.MESSAGE m JOIN dbo.USER u ON m.user_id = u.id JOIN dbo.channel c ON m.channel_id = c.id 
            WHERE m.channel_id = :channelId LIMIT :limit OFFSET :skip;
            """,
        ).bind("channelId", channel.id)
            .bind("limit", limit)
            .bind("skip", skip)
            .map { rs, _ -> mapRowToMessage(rs) }
            .list()
    }

    override fun deleteMessageById(message: Message): Message {
        handle.createUpdate(
            """
            DELETE FROM dbo.message WHERE id = :id
            """,
        ).bind("id", message.id)
            .executeAndReturnGeneratedKeys()
        return message
    }

    override fun deleteMessagesByChannel(channelId: Int): Boolean {
        handle.createUpdate(
            """
            DELETE FROM dbo.message WHERE channel_id = :channelId
            """,
        ).bind("channelId", channelId)
            .executeAndReturnGeneratedKeys()
        return true
    }

    override fun findAll(): List<Message> =
        handle.createQuery(
            """
            SELECT * FROM dbo.message
            """,
        ).map { rs, _ -> mapRowToMessage(rs) }
            .list()

    override fun clear() {
        handle.createUpdate(
            """
            DELETE FROM dbo.message
            """,
        ).execute()
    }

    private fun mapRowToMessage(rs: ResultSet): Message {
        val user =
            User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("email"),
            )
        return Message(
            id = rs.getInt("id"),
            sender = user,
            channel =
                Channel(
                    id = rs.getInt("channel_id"),
                    name = rs.getString("channel_name"),
                    visibility = Visibility.valueOf(rs.getString("visibility")),
                    creator = user,
                ),
            content = rs.getString("message"),
            timestamp = rs.getTimestamp("creationtime").toLocalDateTime(),
        )
    }
}
