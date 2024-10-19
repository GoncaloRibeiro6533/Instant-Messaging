package pt.isel

import kotlinx.datetime.Instant

sealed class Invitation(
    val id: Int,
    val sender: User,
    val isUsed: Boolean,
    val timestamp: Instant,
) {
    abstract fun markAsUsed(): Invitation
}
