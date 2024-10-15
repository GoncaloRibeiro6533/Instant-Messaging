package models.channel

import Visibility

data class ChannelInputModel(val name: String, val creatorId: Int, val visibility: Visibility)

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
