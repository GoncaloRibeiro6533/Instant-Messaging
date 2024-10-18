package pt.isel.models.channel

import pt.isel.Visibility

data class CreateChannelInputModel(
    val name: String,
    val visibility: Visibility,
)

data class ChannelOutputModel(
    val id: Int,
    val name: String,
    val creator: String,
    val visibility: Visibility,
)

data class ChannelList(
    val nChannels: Int,
    val channels: List<ChannelOutputModel>,
)
