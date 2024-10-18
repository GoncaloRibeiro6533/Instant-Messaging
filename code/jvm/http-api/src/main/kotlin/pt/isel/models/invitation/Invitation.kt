package pt.isel.models.invitation

import pt.isel.Role
import pt.isel.models.channel.ChannelOutputModel
import pt.isel.models.user.UserIdentifiers
import java.time.LocalDateTime

data class InvitationInputModel(
    val receiverId: Int,
    val email: String,
    val channelId: Int,
    val role: Role,
)

data class InvitationOutputModel(
    val id: Int,
    val sender: UserIdentifiers,
    val receiverId: Int,
    val channel: ChannelOutputModel,
    val role: Role,
    val timestamp: LocalDateTime,
)

data class InvitationsList(
    val nInvitations: Int,
    val invitations: List<InvitationOutputModel>,
)
