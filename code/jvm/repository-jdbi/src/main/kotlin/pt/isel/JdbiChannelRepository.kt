package pt.isel

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class JdbiChannelRepository(
    private val handle: Handle,
) : ChannelRepository {
    override fun findById(id: Int): Channel? {
        return handle.createQuery("SELECT * FROM dbo.channel WHERE id = :id")
            .bind("id", id)
            .mapTo(Channel::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun getChannelByName(
        name: String,
        limit: Int,
        skip: Int,
    ): List<Channel> {
        return handle.createQuery("SELECT * FROM dbo.channel WHERE name = :name LIMIT :limit OFFSET :skip")
            .bind("name", name)
            .bind("limit", limit)
            .bind("skip", skip)
            .mapTo(Channel::class.java)
            .list()
    }

    override fun createChannel(
        name: String,
        creator: User,
        visibility: Visibility,
    ): Channel {
        return handle.createUpdate("INSERT INTO dbo.channel (name, creator_id, visibility) VALUES (:name, :creator_id, :visibility)")
            .bind("name", name)
            .bind("creator_id", creator.id)
            .bind("visibility", visibility)
            .executeAndReturnGeneratedKeys()
            .map(ChannelMapper(creator))
            .one()
    }

    override fun getChannelsOfUser(user: User): List<Channel> {
        return handle.createQuery("SELECT * FROM dbo.channel WHERE creator_id = :creator_id")
            .bind("creator_id", user.id)
            .mapTo(Channel::class.java)
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

    private class ChannelMapper(private val user: User) : RowMapper<Channel> {
        override fun map(
            rs: ResultSet,
            ctx: StatementContext,
        ): Channel {
            return Channel(
                id = rs.getInt("id"),
                name = rs.getString("name"),
                creator = user,
                visibility = Visibility.valueOf(rs.getString("visibility")),
            )
        }
    }
}
