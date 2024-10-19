package pt.isel

import org.jdbi.v3.core.Handle
import java.sql.ResultSet

class JdbiChannelRepository(
    private val handle: Handle,
) : ChannelRepository {
    override fun findById(id: Int): Channel? {
        return handle.createQuery(
            """
            SELECT c.*, u.username, u.email FROM dbo.CHANNEL c JOIN dbo.USER u ON c.creator_id = u.id WHERE c.id = :id;
            """.trimIndent(),
        )
            .bind("id", id)
            .map { rs, _ -> mapRowToChannel(rs) }
            .findOne()
            .orElse(null)
    }

    override fun getChannelByName(
        name: String,
        limit: Int,
        skip: Int,
    ): List<Channel> {
        return handle.createQuery(
            """
            SELECT c.*, u.username, u.email FROM dbo.CHANNEL c 
            JOIN dbo.USER u ON c.creator_id = u.id WHERE UPPER(c.name) LIKE UPPER(:name) || '%' LIMIT :limit OFFSET :skip;
            """.trimIndent(),
        )
            .bind("name", name)
            .bind("limit", limit)
            .bind("skip", skip)
            .map { rs, _ -> mapRowToChannel(rs) }
            .list()
    }

    override fun createChannel(
        name: String,
        creator: User,
        visibility: Visibility,
    ): Channel {
        val id =
            handle.createUpdate("INSERT INTO dbo.channel (name, creator_id, visibility) VALUES (:name, :creator_id, :visibility)")
                .bind("name", name)
                .bind("creator_id", creator.id)
                .bind("visibility", visibility)
                .executeAndReturnGeneratedKeys()
        return Channel(
            id.mapTo(Int::class.java).one(),
            name,
            creator,
            visibility,
        )
    }

    override fun getChannelsOfUser(user: User): List<Channel> {
        return handle.createQuery(
            """
                SELECT 
                    ucr.*, 
                    u.username, 
                    u.email,
                    ch.creator_id, 
                    ch.name AS name,
                    ch.visibility,
                    ch.id
                FROM 
                    dbo.USER_CHANNEL_ROLE ucr
                JOIN 
                    dbo.USER u ON ucr.user_id = u.id
                JOIN 
                    dbo.CHANNEL ch ON ucr.channel_id = ch.id
                WHERE 
                    ucr.user_id = :user_id;
            """.trimIndent(),
        )
            .bind("user_id", user.id)
            .map { rs, _ -> mapRowToChannel(rs) }
            .list()
    }

    override fun getChannelMembers(channel: Channel): List<Int> {
        return handle.createQuery("SELECT user_id FROM dbo.user_channel_role WHERE channel_id = :channel_id")
            .bind("channel_id", channel.id)
            .mapTo(Int::class.java)
            .list()
    }

    override fun addUserToChannel(
        user: User,
        channel: Channel,
        role: Role,
    ): Channel {
        return handle.createUpdate(
            "INSERT INTO dbo.user_channel_role (user_id, channel_id, role_name) VALUES (:user_id, :channel_id, :role)",
        )
            .bind("user_id", user.id)
            .bind("channel_id", channel.id)
            .bind("role", role)
            .execute()
            .let { channel }
    }

    override fun updateChannelName(
        channel: Channel,
        name: String,
    ): Channel {
        return handle.createUpdate("UPDATE dbo.channel SET name = :name WHERE id = :id")
            .bind("name", name)
            .bind("id", channel.id)
            .execute()
            .let { channel.copy(name = name) }
    }

    override fun leaveChannel(
        user: User,
        channel: Channel,
    ): Channel {
        return handle.createUpdate("DELETE FROM dbo.user_channel_role WHERE user_id = :user_id AND channel_id = :channel_id")
            .bind("user_id", user.id)
            .bind("channel_id", channel.id)
            .execute()
            .let { channel }
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM dbo.channel")
            .execute()
    }

    private fun mapRowToChannel(rs: ResultSet): Channel {
        val user =
            User(
                rs.getInt("creator_id"),
                rs.getString("username"),
                rs.getString("email"),
            )
        return Channel(
            rs.getInt("id"),
            rs.getString("name"),
            user,
            Visibility.valueOf(rs.getString("visibility")),
        )
    }
}
