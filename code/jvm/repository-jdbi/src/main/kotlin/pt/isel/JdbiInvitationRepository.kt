package pt.isel

import org.jdbi.v3.core.Handle
import java.sql.ResultSet
import java.time.LocalDateTime

class JdbiInvitationRepository(
    private val handle: Handle,
) : InvitationRepository {
    override fun createRegisterInvitation(
        sender: User,
        email: String,
        channel: Channel,
        role: Role,
        timestamp: LocalDateTime,
        code: String,
    ): RegisterInvitation {
        val id =
            handle.createUpdate(
                """
                INSERT INTO dbo.REGISTER_INVITATION (role_name, used, channel_id, invited_email, inviter_id, timestamp
                , code)
                VALUES (:role, :used, :channel_id, :email, :sender_id, :timestamp, :code)
                """.trimIndent(),
            )
                .bind("sender_id", sender.id)
                .bind("email", email)
                .bind("channel_id", channel.id)
                .bind("used", false)
                .bind("role", role)
                .bind("timestamp", timestamp)
                .bind("code", code)
                .executeAndReturnGeneratedKeys().mapTo(Int::class.java).one()

        return RegisterInvitation(
            id,
            sender,
            email,
            channel,
            role,
            false,
            timestamp,
            code,
        )
    }

    override fun createChannelInvitation(
        sender: User,
        receiver: User,
        channel: Channel,
        role: Role,
        timestamp: LocalDateTime,
    ): ChannelInvitation {
        val id =
            handle.createUpdate(
                """
                INSERT INTO dbo.CHANNEL_INVITATION (role_name, used, channel_id, invited_id, inviter_id, timestamp)
                VALUES (:role, :used, :channel_id, :receiver_id, :sender_id, :timestamp)
                """.trimIndent(),
            )
                .bind("sender_id", sender.id)
                .bind("receiver_id", receiver.id)
                .bind("channel_id", channel.id)
                .bind("used", false)
                .bind("role", role)
                .bind("timestamp", timestamp)
                .executeAndReturnGeneratedKeys().mapTo(Int::class.java).one()
        return ChannelInvitation(
            id,
            sender,
            receiver,
            channel,
            role,
            false,
            timestamp,
        )
    }

    override fun findRegisterInvitationById(invitationId: Int): RegisterInvitation? {
        return handle.createQuery(
            """
            SELECT 
                ri.id,
                ri.role_name,
                ri.used,
                ri.channel_id,
                ri.invited_email,
                ri.inviter_id,
                ri.timestamp,
                ri.code,
                inviter.username AS inviter_username,
                inviter.email AS inviter_email,
                ch.name AS channel_name,
                ch.visibility,
                ch.creator_id,
                creator.username AS creator_username,
                creator.email AS creator_email
            FROM dbo.REGISTER_INVITATION ri
            JOIN dbo.USER inviter ON inviter.id = ri.inviter_id
            LEFT JOIN dbo.CHANNEL ch ON ch.id = ri.channel_id
            LEFT JOIN dbo.USER creator ON creator.id = ch.creator_id
            WHERE ri.id = :id;
            """.trimIndent(),
        )
            .bind("id", invitationId)
            .map { rs, _ -> mapRowToRegisterInvitation(rs) }
            .findFirst()
            .orElse(null)
    }

    override fun findChannelInvitationById(invitationId: Int): ChannelInvitation? {
        return handle.createQuery(
            """    
            SELECT 
                ci.id,
                ci.role_name,
                ci.used,
                ci.channel_id,
                ci.inviter_id,
                ci.invited_id,
                ci.timestamp,
                inviter.username AS inviter_username,
                inviter.email AS inviter_email,
                invited.username AS invited_username,
                invited.email AS invited_email,
                ch.name AS channel_name,
                ch.visibility,
                ch.creator_id,
                creator.username AS creator_username,
                creator.email AS creator_email
            FROM dbo.CHANNEL_INVITATION ci
            JOIN dbo.USER inviter ON inviter.id = ci.inviter_id
            JOIN dbo.USER invited ON invited.id = ci.invited_id
            JOIN dbo.CHANNEL ch ON ch.id = ci.channel_id
            JOIN dbo.USER creator ON creator.id = ch.creator_id
            WHERE ci.id = :id; 
            """.trimIndent(),
        )
            .bind("id", invitationId)
            .map { rs, _ -> mapRowToChannelInvitation(rs) }
            .findFirst()
            .orElse(null)
    }

    override fun updateRegisterInvitation(invitation: RegisterInvitation): RegisterInvitation {
        handle.createUpdate(
            """
            UPDATE dbo.REGISTER_INVITATION
            SET used = :used
            WHERE id = :id
            """,
        )
            .bind("used", true)
            .bind("id", invitation.id)
            .execute()
        return invitation.markAsUsed()
    }

    override fun updateChannelInvitation(invitation: ChannelInvitation): ChannelInvitation {
        handle.createUpdate(
            """
            UPDATE dbo.CHANNEL_INVITATION
            SET used = :used
            WHERE id = :id
            """,
        )
            .bind("used", true)
            .bind("id", invitation.id)
            .execute()
        return invitation.markAsUsed()
    }

    override fun deleteRegisterInvitationById(invitationId: Int): Boolean {
        val rowsAffected =
            handle.createUpdate(
                """
        DELETE FROM dbo.REGISTER_INVITATION
        WHERE id = :id
        """,
            )
                .bind("id", invitationId)
                .execute()
        return rowsAffected > 0
    }

    override fun deleteChannelInvitationById(invitationId: Int): Boolean {
        val affectedRows =
            handle.createUpdate(
                """
            DELETE FROM dbo.CHANNEL_INVITATION
            WHERE id = :id
            """,
            )
                .bind("id", invitationId)
                .execute()
        return affectedRows > 0
    }

    override fun getInvitationsOfUser(user: User): List<ChannelInvitation> {
        return handle.createQuery(
            """
            SELECT 
                 ci.id,
                 ci.role_name,
                 ci.used,
                 ci.channel_id,
                 ci.inviter_id,
                 ci.invited_id,
                 ci.timestamp,
                 inviter.username AS inviter_username,
                 inviter.email AS inviter_email,
                 invited.username AS invited_username,
                 invited.email AS invited_email,
                 ch.name AS channel_name,
                 ch.visibility,
                 ch.creator_id,
                 creator.username AS creator_username,
                 creator.email AS creator_email
             FROM dbo.CHANNEL_INVITATION ci
             JOIN dbo.USER inviter ON inviter.id = ci.inviter_id
             JOIN dbo.USER invited ON invited.id = ci.invited_id
             JOIN dbo.CHANNEL ch ON ch.id = ci.channel_id
             JOIN dbo.USER creator ON creator.id = ch.creator_id
             WHERE ci.invited_id = :user_id AND ci.used = FALSE;
            """.trimIndent(),
        )
            .bind("user_id", user.id)
            .map { rs, _ -> mapRowToChannelInvitation(rs) }
            .list()
    }

    override fun findRegisterInvitationByCode(code: String): RegisterInvitation? {
        return handle.createQuery(
            """
            SELECT 
                ri.id,
                ri.role_name,
                ri.used,
                ri.channel_id,
                ri.invited_email,
                ri.inviter_id,
                ri.timestamp,
                ri.code,
                inviter.username AS inviter_username,
                inviter.email AS inviter_email,
                ch.name AS channel_name,
                ch.visibility,
                ch.creator_id,
                creator.username AS creator_username,
                creator.email AS creator_email
            FROM dbo.REGISTER_INVITATION ri
            JOIN dbo.USER inviter ON inviter.id = ri.inviter_id
            LEFT JOIN dbo.CHANNEL ch ON ch.id = ri.channel_id
            LEFT JOIN dbo.USER creator ON creator.id = ch.creator_id
            WHERE ri.code = :code;
            """.trimIndent(),
        )
            .bind("code", code)
            .map { rs, _ -> mapRowToRegisterInvitation(rs) }
            .findFirst()
            .orElse(null)
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM dbo.REGISTER_INVITATION")
            .execute()
        handle.createUpdate("DELETE FROM dbo.CHANNEL_INVITATION")
            .execute()
    }

    private fun mapRowToChannelInvitation(rs: ResultSet): ChannelInvitation {
        val userSender =
            User(
                rs.getInt("inviter_id"),
                rs.getString("inviter_username"),
                rs.getString("inviter_email"),
            )
        val userReceiver =
            User(
                rs.getInt("invited_id"),
                rs.getString("invited_username"),
                rs.getString("invited_email"),
            )
        val channelCreator =
            User(
                rs.getInt("creator_id"),
                rs.getString("creator_username"),
                rs.getString("creator_email"),
            )
        val channel =
            Channel(
                id = rs.getInt("channel_id"),
                name = rs.getString("channel_name"),
                visibility = Visibility.valueOf(rs.getString("visibility")),
                creator = channelCreator,
            )
        return ChannelInvitation(
            id = rs.getInt("id"),
            sender = userSender,
            receiver = userReceiver,
            channel = channel,
            role = Role.valueOf(rs.getString("role_name")),
            isUsed = rs.getBoolean("used"),
            timestamp = rs.getTimestamp("timestamp").toLocalDateTime(),
        )
    }

    private fun mapRowToRegisterInvitation(rs: ResultSet): RegisterInvitation {
        val userSender =
            User(
                rs.getInt("inviter_id"),
                rs.getString("inviter_username"),
                rs.getString("inviter_email"),
            )
        val channelCreator =
            User(
                rs.getInt("creator_id"),
                rs.getString("creator_username"),
                rs.getString("creator_email"),
            )
        val channel =
            Channel(
                id = rs.getInt("channel_id"),
                name = rs.getString("channel_name"),
                visibility = Visibility.valueOf(rs.getString("visibility")),
                creator = channelCreator,
            )
        return RegisterInvitation(
            id = rs.getInt("id"),
            sender = userSender,
            email = rs.getString("invited_email"),
            channel = channel,
            role = Role.valueOf(rs.getString("role_name")),
            isUsed = rs.getBoolean("used"),
            timestamp = rs.getTimestamp("timestamp").toLocalDateTime(),
            code = rs.getString("code"),
        )
    }
}
