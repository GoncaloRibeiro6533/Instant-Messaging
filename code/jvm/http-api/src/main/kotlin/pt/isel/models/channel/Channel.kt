package pt.isel.models.channel

import pt.isel.Role
import pt.isel.Visibility
import pt.isel.models.user.UserIdentifiers

data class CreateChannelInputModel(
    val name: String,
    val visibility: Visibility,
)

data class ChannelOutputModel(
    val id: Int,
    val name: String,
    val creator: UserIdentifiers,
    val visibility: Visibility,
)

data class ChannelList(
    val nChannels: Int,
    val channels: List<ChannelOutputModel>,
)


data class ChannelMember(
    val user: UserIdentifiers,
    val role: Role,
)

data class ChannelMembersList(
    val nMembers: Int,
    val members: List<ChannelMember>,
)

data class ChannelOfUser(
    val channel: ChannelOutputModel,
    val role: Role,
)

data class ChannelOfUserList(
    val nChannels: Int,
    val channels: List<ChannelOfUser>,
)
