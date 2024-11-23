package pt.isel

import org.jdbi.v3.core.Handle
import java.sql.ResultSet

class JdbiChannelRepository(
    private val handle: Handle,
) : ChannelRepository {
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
                .mapTo(Int::class.java)
                .one()
        return Channel(id, name, creator, visibility)
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
        val query =
            if (limit == 1 && skip == 0) {
                """
            SELECT c.*, u.username, u.email FROM dbo.CHANNEL c 
            JOIN dbo.USER u ON c.creator_id = u.id WHERE c.name LIKE :name || '%' LIMIT :limit OFFSET :skip;
            """
            } else {
                """
            SELECT c.*, u.username, u.email FROM dbo.CHANNEL c 
            JOIN dbo.USER u ON c.creator_id = u.id WHERE UPPER(c.name) LIKE UPPER(:name) || '%' ORDER BY c.name LIMIT :limit OFFSET :skip;
            """
            }
        return handle.createQuery(
            query.trimIndent(),
        )
            .bind("name", name)
            .bind("limit", limit)
            .bind("skip", skip)
            .map { rs, _ -> mapRowToChannel(rs) }
            .list()
    }

    // TODO add role
    override fun getChannelsOfUser(user: User): List<Channel> {
        return handle.createQuery(
            """
            SELECT 
                ucr.*, 
                u_creator.username AS username, 
                u_creator.email AS email,
                ch.creator_id, 
                ch.name,
                ch.visibility,
                ch.id
            FROM 
                dbo.USER_CHANNEL_ROLE ucr
            JOIN 
                dbo.CHANNEL ch ON ucr.channel_id = ch.id
            JOIN 
                dbo.USER u_creator ON ch.creator_id = u_creator.id
            WHERE 
                ucr.user_id = :user_id;
            """.trimIndent(),
        )
            .bind("user_id", user.id)
            .map { rs, _ -> mapRowToChannel(rs) }
            .list()
    }

    override fun getChannelMembers(channel: Channel): Map<User, Role> {
        return handle.createQuery(
            """
            SELECT 
                ur.user_id AS user_id,
                ur.channel_id AS channel_id,
                ur.role_name AS ROLE,
                u.username AS user_username,
                u.email AS user_email
            FROM dbo.user_channel_role ur 
            JOIN dbo.USER u ON u.id = user_id
            WHERE ur.channel_id = :channel_id;
            """.trimIndent(),
        ).bind("channel_id", channel.id)
            .map { rs, _ -> mapRowToUserRoleEntry(rs) }
            .list()
            .toMap()
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
        handle.createUpdate("DELETE FROM dbo.user_channel_role WHERE user_id = :user_id AND channel_id = :channel_id")
            .bind("user_id", user.id)
            .bind("channel_id", channel.id)
            .execute()

        return channel
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

    private fun mapRowToUserRoleEntry(rs: ResultSet): Pair<User, Role> {
        val user =
            User(
                rs.getInt("user_id"),
                rs.getString("user_username"),
                rs.getString("user_email"),
            )
        return user to Role.valueOf(rs.getString("role"))
    }
}
