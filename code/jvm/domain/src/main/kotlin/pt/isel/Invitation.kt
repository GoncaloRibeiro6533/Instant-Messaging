package pt.isel

import java.time.LocalDateTime

sealed class Invitation(
    val id: Int,
    val sender: User,
    val isUsed: Boolean,
    val timestamp: LocalDateTime,
) {
    abstract fun markAsUsed(): Invitation
}
