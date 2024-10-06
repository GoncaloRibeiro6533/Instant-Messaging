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
        val user = MockChannelRepository.users[0]
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
        val user = MockChannelRepository.users[0]
        val result = channelServices.getChannelsOfUser(user.id)
        assertEquals(MockChannelRepository.channels, result)
    }

    @Test
    fun `Test getChannelById with negative ID`() {
        val exception = assertFailsWith<Errors.BadRequestException> {
            channelServices.getChannelById(-1)
        }
        assertEquals("Id must be greater than 0", exception.message)
    }

    @Test
    fun `Test getChannelByName with blank name`() {
        val exception = assertFailsWith<Errors.BadRequestException> {
            channelServices.getChannelByName("")
        }
        assertEquals("Channel name must not be blank", exception.message)
    }

    @Test
    fun `Test createChannel with blank name`() {
        val exception = assertFailsWith<Errors.BadRequestException> {
            channelServices.createChannel("", 1, Visibility.PUBLIC)
        }
        assertEquals("Channel name must not be blank", exception.message)
    }


    @Test
    fun `Test getMsgHistory with negative channel ID`() {
        val exception = assertFailsWith<Errors.BadRequestException> {
            channelServices.getMsgHistory(-1)
        }
        assertEquals("Channel id must be greater than 0", exception.message)
    }

    @Test
    fun `Test getChannelsOfUser with negative user ID`() {
        val exception = assertFailsWith<Errors.BadRequestException> {
            channelServices.getChannelsOfUser(-1)
        }
        assertEquals("User id must be greater than 0", exception.message)
    }

    @Test
    fun `Test getChannelById with non-existent ID`() {
        val exception = assertFailsWith<Errors.NotFoundException> {
            channelServices.getChannelById(999)
        }
        assertEquals("Channel not found", exception.message)
    }

    @Test
    fun `Test getChannelByName with non-existent name`() {
        val exception = assertFailsWith<Errors.NotFoundException> {
            channelServices.getChannelByName("nonExistentChannel")
        }
        assertEquals("Channel not found", exception.message)
    }

    @Test
    fun `Test createChannel with existing name`() {
        val existingChannelName = MockChannelRepository.channels[0].name
        val exception = assertFailsWith<Errors.BadRequestException> {
            channelServices.createChannel(existingChannelName, 1, Visibility.PUBLIC)
        }
        assertEquals("Channel already exists", exception.message)
    }

    @Test
    fun `getChannelMembers should return members for valid channel ID`() {
        val channel = MockChannelRepository.channels[0]
        val result = channelServices.getChannelMembers(channel.id)
        assertEquals(MockChannelRepository.users.toList(), result)
    }

    @Test
    fun `getChannelMembers should throw exception for negative channel ID`() {
        val exception = assertFailsWith<Errors.BadRequestException> {
            channelServices.getChannelMembers(-1)
        }
        assertEquals("Channel id must be greater than 0", exception.message)
    }

    @Test
    fun `getChannelMembers should throw exception for non-existent channel ID`() {
        val exception = assertFailsWith<Errors.NotFoundException> {
            channelServices.getChannelMembers(999)
        }
        assertEquals("Channel not found", exception.message)
    }

    @Test
    fun `getChannelsOfUser should return channels for valid user ID`() {
        val user = MockChannelRepository.users[0]
        val result = channelServices.getChannelsOfUser(user.id)
        assertEquals(MockChannelRepository.channels, result)
    }

    @Test
    fun `getChannelsOfUser should throw exception for negative user ID`() {
        val exception = assertFailsWith<Errors.BadRequestException> {
            channelServices.getChannelsOfUser(-1)
        }
        assertEquals("User id must be greater than 0", exception.message)
    }

    @Test
    fun `getChannelsOfUser should throw exception for non-existent user ID`() {
        val exception = assertFailsWith<Errors.NotFoundException> {
            channelServices.getChannelsOfUser(999)
        }
        assertEquals("User not found", exception.message)
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