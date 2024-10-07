import mocks.MockChannelRepository


import kotlin.test.*

class ChannelServicesTests {

    private val mock = MockChannelRepository()
    private val channelServices = ChannelServices(mock)

    @Test
    fun `Test to get a channel ID` () {
        val channel = MockChannelRepository.channels[0]
        val result = channelServices.getChannelById(channel.id)
        assertIs<Success<Channel>>(result)
        assertEquals(channel, result.value)
    }


    @Test
    fun `Test to get a channel by name` () {
        val channel = MockChannelRepository.channels[0]
        val result = channelServices.getChannelByName(channel.name)
        assertIs<Success<Channel>>(result)
        assertEquals(channel, result.value)
    }

    @Test
    fun `Test to create a channel` () {
        val name = "channel2"
        val visibility = Visibility.PUBLIC
        val user = MockChannelRepository.users[0]
        val result = channelServices.createChannel(name, user.id, visibility)
        assertIs<Success<Channel>>(result)
        assertEquals(name, result.value.name)
        assertEquals(visibility, result.value.visibility)
    }

    @Test
    fun `Test to get messages history` () {
        val channel = MockChannelRepository.channels[0]
        val limit = 5
        val skip = 5
        val result = channelServices.getMsgHistory(channel.id, limit, skip)
        assertIs<Success<List<Message>>>(result)
        assertEquals(emptyList<Message>(), result.value)
    }

    @Test
    fun `Test to get channels of a user` () {
        val user = MockChannelRepository.users[0]
        val result = channelServices.getChannelsOfUser(user.id)
        assertIs<Success<List<Channel>>>(result)
        assertEquals(MockChannelRepository.channels, result.value)
    }

    @Test
    fun `Test getChannelById with negative ID`() {
        val exception = channelServices.getChannelById(-1)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.NegativeIdentifier, exception.value)
    }

    @Test
    fun `Test getChannelByName with blank name`() {
        val exception = channelServices.getChannelByName("")
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.InvalidChannelName, exception.value)
    }


    @Test
    fun `Test createChannel with blank name`() {
        val exception = channelServices.createChannel("", 1, Visibility.PUBLIC)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.InvalidChannelName, exception.value)
    }


    @Test
    fun `Test getMsgHistory with negative channel ID`() {
        val exception = channelServices.getMsgHistory(-1)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.NegativeIdentifier, exception.value)
    }

    @Test
    fun `Test getChannelsOfUser with negative user ID`() {
        val exception = channelServices.getChannelsOfUser(-1)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.NegativeIdentifier, exception.value)
    }

    @Test
    fun `Test getChannelById with non-existent ID`() {
        val exception = channelServices.getChannelById(999)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.ChannelNotFound, exception.value)
    }

    @Test
    fun `Test getChannelByName with non-existent name`() {
        val exception = channelServices.getChannelByName("nonExistentChannel")
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.ChannelNotFound, exception.value)
    }

    @Test
    fun `Test createChannel with existing name`() {
        val existingChannelName = MockChannelRepository.channels[0].name
        val exception = channelServices.createChannel(existingChannelName, 1, Visibility.PUBLIC)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.ChannelAlreadyExists, exception.value)
    }

    @Test
    fun `getChannelMembers should return members for valid channel ID`() {
        val channel = MockChannelRepository.channels[0]
        val result = channelServices.getChannelMembers(channel.id)
        assertIs<Success<List<User>>>(result)
        assertEquals(MockChannelRepository.users.toList(), result.value)
    }

    @Test
    fun `getChannelMembers should throw exception for negative channel ID`() {
        val exception = channelServices.getChannelMembers(-1)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.NegativeIdentifier, exception.value)
    }

    @Test
    fun `getChannelMembers should throw exception for non-existent channel ID`() {
        val exception = channelServices.getChannelMembers(999)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.ChannelNotFound, exception.value)
    }

    @Test
    fun `getChannelsOfUser should return channels for valid user ID`() {
        val user = MockChannelRepository.users[0]
        val result = channelServices.getChannelsOfUser(user.id)
        assertIs<Success<List<Channel>>>(result)
        assertEquals(MockChannelRepository.channels, result.value)
    }

    @Test
    fun `getChannelsOfUser should throw exception for negative user ID`() {
        val exception = channelServices.getChannelsOfUser(-1)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.NegativeIdentifier, exception.value)
    }

    @Test
    fun `getChannelsOfUser should throw exception for non-existent user ID`() {
        val exception = channelServices.getChannelsOfUser(999)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.ChannelNotFound, exception.value)
    }

    /*
    @Test
    fun `Test createChannel with invalid visibility`() {
        val exception = assertFailsWith<Errors.BadRequestException> {
            channelServices.createChannel("channel", 1, Visibility.valueOf("ABC"))
        }
        assertEquals("Invalid visibility", exception.message)
    }
     */



}