package models

import Role
import java.time.LocalDateTime

data class InvitationInputModel(
    val senderId: Int,
    val receiverId: Int,
    val email: String?,
    val channelId: Int?,
    val role: Role,
)

data class InvitationOutputModel(
    val id: Int,
    val senderId: Int,
    val receiverId: Int,
    val email: String?,
    val channelId: Int?,
    val isUsed: Boolean,
    val timestamp: LocalDateTime,
)
