package pt.isel.models.invitation

import pt.isel.Role
import pt.isel.models.channel.ChannelOutputModel
import pt.isel.models.user.UserIdentifiers
import java.time.LocalDateTime

data class InvitationInputModelChannel(
    val receiverId: Int,
    val channelId: Int,
    val role: Role,
)

data class InvitationOutputModelChannel(
    val id: Int,
    val sender: UserIdentifiers,
    val receiver: UserIdentifiers,
    val channel: ChannelOutputModel,
    val role: Role,
    val timestamp: LocalDateTime,
)

data class InvitationInputModelRegister(
    val email: String,
    val channelId: Int,
    val role: Role,
)

data class InvitationOutputModelRegister(
    val id: Int,
    val sender: UserIdentifiers,
    val email: String,
    val channel: ChannelOutputModel,
    val role: Role,
    val timestamp: LocalDateTime,
)

data class InvitationsList(
    val nInvitations: Int,
    val invitations: List<InvitationOutputModelChannel>,
)
