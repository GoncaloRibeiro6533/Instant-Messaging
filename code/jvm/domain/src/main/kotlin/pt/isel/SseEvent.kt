@file:Suppress("ktlint")
package pt.isel

import kotlinx.datetime.Instant

sealed interface SseEvent {

    data class NewChannelMessage(
        val id: Long, // SSE Event identifier
        val message: Message, // New channel message
    ) : SseEvent

    data class ChannelNameUpdate(
        val id: Long, // SSE Event identifier
        val channel: Channel, // New channel
    ) : SseEvent

    data class ChannelNewMemberUpdate(
        val id: Long, // SSE Event identifier
        val newMember: NewMember,
    ) : SseEvent

    data class ChannelMemberExitedUpdate(
        val id: Long, // SSE Event identifier
        val removedMember: RemovedMember,
    ) : SseEvent

    data class NewInvitationUpdate(
        val id: Long, // SSE Event identifier
        val invitation: ChannelInvitation, // New invitation
    ) : SseEvent

    data class InvitationAcceptedUpdate(
        val id: Long, // SSE Event identifier
        val invitation: ChannelInvitation, // Accepted invitation
    ) : SseEvent

    data class MemberUsernameUpdate(
        val id: Long,
        val updatedMember: User
    ): SseEvent

    data class KeepAlive(
        val timestamp: Instant,
    ) : SseEvent
}

data class NewMember(
    val channel: Channel, // Channel with updated members
    val newMember: User,
    val role : Role
)

data class RemovedMember(
    val channel: Channel, // Channel with updated members
    val removedMember: User
)

