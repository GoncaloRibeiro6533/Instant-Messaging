import org.jdbi.v3.core.Handle

class JdbiInvitationRepository(
    private val handle: Handle,
) : InvitationRepository {
    override fun createRegisterInvitation(
        sender: User,
        email: String,
        channel: Channel,
        role: Role,
    ): RegisterInvitation {
        return handle.createQuery(
            """
            INSERT INTO dbo.REGISTER_INVITATION (role_name, used, channel_id, invited_email, inviter_id)
            VALUES (:role, :used, :channel_id, :email, :sender)
            """,
        )
            .bind("sender_id", sender.id)
            .bind("email", email)
            .bind("channel_id", channel.id)
            .bind("used", false)
            .bind("role", role)
            .mapTo(RegisterInvitation::class.java)
            .one()
    }

    override fun createChannelInvitation(
        sender: User,
        receiver: User,
        channel: Channel,
        role: Role,
    ): ChannelInvitation {
        return handle.createQuery(
            """
            INSERT INTO dbo.CHANNEL_INVITATION (role_name, used, channel_id, invited_id, inviter_id)
            VALUES (:role, :used, :channel_id, :receiver, :sender)
            """,
        )
            .bind("sender_id", sender.id)
            .bind("receiver_id", receiver.id)
            .bind("channel_id", channel.id)
            .bind("used", false)
            .bind("role", role)
            .mapTo(ChannelInvitation::class.java)
            .one()
    }

    override fun findRegisterInvitationById(invitationId: Int): Invitation? {
        return handle.createQuery(
            """
            SELECT * FROM dbo.REGISTER_INVITATION
            WHERE id = :id
            """,
        )
            .bind("id", invitationId)
            .mapTo(RegisterInvitation::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun findChannelInvitationById(invitationId: Int): Invitation? {
        return handle.createQuery(
            """
            SELECT * FROM dbo.CHANNEL_INVITATION
            WHERE id = :id
            """,
        )
            .bind("id", invitationId)
            .mapTo(ChannelInvitation::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun updateRegisterInvitation(invitation: Invitation): Invitation {
        return handle.createUpdate(
            """
            UPDATE dbo.REGISTER_INVITATION
            SET used = :used
            WHERE id = :id
            """,
        )
            .bind("used", invitation.isUsed)
            .bind("id", invitation.id)
            .executeAndReturnGeneratedKeys()
            .mapTo(RegisterInvitation::class.java)
            .one()
    }

    override fun updateChannelInvitation(invitation: Invitation): Invitation {
        return handle.createUpdate(
            """
            UPDATE dbo.CHANNEL_INVITATION
            SET used = :used
            WHERE id = :id
            """,
        )
            .bind("used", invitation.isUsed)
            .bind("id", invitation.id)
            .executeAndReturnGeneratedKeys()
            .mapTo(ChannelInvitation::class.java)
            .one()
    }

    override fun deleteRegisterInvitationById(invitationId: Int): Invitation {
        return handle.createUpdate(
            """
            DELETE FROM dbo.REGISTER_INVITATION
            WHERE id = :id
            """,
        )
            .bind("id", invitationId)
            .executeAndReturnGeneratedKeys()
            .mapTo(RegisterInvitation::class.java)
            .one()
    }

    override fun deleteChannelInvitationById(invitationId: Int): Invitation {
        return handle.createUpdate(
            """
            DELETE FROM dbo.CHANNEL_INVITATION
            WHERE id = :id
            """,
        )
            .bind("id", invitationId)
            .executeAndReturnGeneratedKeys()
            .mapTo(ChannelInvitation::class.java)
            .one()
    }

    override fun getInvitationsOfUser(user: User): List<Invitation> {
        return handle.createQuery(
            """
            SELECT * FROM dbo.CHANNEL_INVITATION
            WHERE invited_id = :user_id
            """,
        )
            .bind("user_id", user.id)
            .mapTo(ChannelInvitation::class.java)
            .list()
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM dbo.REGISTER_INVITATION")
            .execute()
    }
}
