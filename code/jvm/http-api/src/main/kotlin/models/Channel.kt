package models

import Message
import Role
import User
import Visibility

data class ChannelInputModel(val name: String, val visibility: Visibility)

data class ChannelOutputModel(
    val id: Int,
    val name: String,
    val creatorId: Int,
    val visibility: Visibility,
    val messages: List<Message>,
    val users: Map<User, Role>,
)
