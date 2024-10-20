package pt.isel.models.channel

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

data class ChannelIdentifiers(
    val id: Int,
    val name: String,
)
