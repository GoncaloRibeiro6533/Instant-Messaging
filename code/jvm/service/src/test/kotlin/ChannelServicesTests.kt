import mocks.MockChannelRepository


import kotlin.test.*

class ChannelServicesTests {

    private val mock = MockChannelRepository()
    private val channelServices = ChannelServices(mock)

    @Test
    fun `Test to get a channel ID` () {
        val channel = MockChannelRepository.channels[0]
        val result = channelServices.getChannelById(channel.id)
        assertEquals(channel, result)
    }

    @Test
    fun `Test to get a channel by name` () {
        val channel = MockChannelRepository.channels[0]
        val result = channelServices.getChannelByName(channel.name)
        assertEquals(channel, result)
    }

    @Test
    fun `Test to create a channel` () {
        val name = "channel2"
        val visibility = Visibility.PUBLIC
        val user = MockChannelRepository.users1[0]
        val result = channelServices.createChannel(name, user.id, visibility)
        assertEquals(name, result.name)
        assertEquals(visibility, result.visibility)
    }

    @Test
    fun `Test to get messages history` () {
        val channel = MockChannelRepository.channels[0]
        val limit = 5
        val skip = 5
        val result = channelServices.getMsgHistory(channel.id, limit, skip)
        assertEquals(emptyList(), result)
    }

    @Test
    fun `Test to get channels of a user` () {
        val user = MockChannelRepository.usersWithChannel[0]
        val result = channelServices.getChannelsOfUser(user.id)
        assertEquals(MockChannelRepository.channels, result)
    }


}