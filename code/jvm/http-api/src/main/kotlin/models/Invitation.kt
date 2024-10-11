package models

import java.time.LocalDateTime

data class InvitationInputModel(
    val senderId: Int,
    val receiverId: Int,
    val channelId: Int,
)

data class InvitationOutputModel(
    val id: Int,
    val senderId: Int,
    val receiverId: Int,
    val channelId: Int,
    val isUsed: Boolean,
    val timestamp: LocalDateTime,
)
