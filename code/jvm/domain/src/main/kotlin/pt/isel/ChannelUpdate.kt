@file:Suppress("ktlint")
package pt.isel

import kotlinx.datetime.Instant

sealed interface ChannelUpdate {

    data class NewChannelMessage(
        val id: Long, // SSE Event identifier
        val message: Message, // New channel message
    ) : ChannelUpdate

    data class ChannelNameUpdate(
        val id: Long, // SSE Event identifier
        val channel: Channel, // New channel
    ) : ChannelUpdate

    data class ChannelNewMemberUpdate(
        val id: Long, // SSE Event identifier
        val channel: Channel, // Channel with updated members
        val newMember: User,
        val role : Role
    ) : ChannelUpdate

    data class ChannelMemberExitedUpdate(
        val id: Long, // SSE Event identifier
        val channel: Channel, // Channel with updated members
        val removedMember: User,
    ) : ChannelUpdate


    data class KeepAlive(
        val timestamp: Instant,
    ) : ChannelUpdate
}