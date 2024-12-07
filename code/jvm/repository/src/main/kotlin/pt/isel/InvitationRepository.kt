package pt.isel

import java.time.LocalDateTime

interface InvitationRepository {
    fun createRegisterInvitation(
        sender: User,
        email: String,
        channel: Channel,
        role: Role,
        timestamp: LocalDateTime,
        code: String,
    ): RegisterInvitation

    fun createChannelInvitation(
        sender: User,
        receiver: User,
        channel: Channel,
        role: Role,
        timestamp: LocalDateTime,
    ): ChannelInvitation

    fun findRegisterInvitationById(invitationId: Int): RegisterInvitation?

    fun findChannelInvitationById(invitationId: Int): ChannelInvitation?

    fun updateRegisterInvitation(invitation: RegisterInvitation): RegisterInvitation

    fun updateChannelInvitation(invitation: ChannelInvitation): ChannelInvitation

    fun deleteRegisterInvitationById(invitationId: Int): Boolean

    fun deleteChannelInvitationById(invitationId: Int): Boolean

    fun getInvitationsOfUser(user: User): List<ChannelInvitation>

    fun findRegisterInvitationByCode(code: String): RegisterInvitation?

    fun clear()
}
