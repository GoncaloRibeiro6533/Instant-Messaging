import mocks.MockChannelRepository
import mocks.MockUserRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ChannelRepoMockTests {

    private val repoUsers = MockUserRepository()

    private val repoChannels = MockChannelRepository()

    @Test
    fun `Test create channel`() {
        val user = repoUsers.create("user1", "email@email.com", "password", "token")
        val channel = repoChannels.createChannel("channel1", user.id, Visibility.PUBLIC)
        assertEquals("channel1", channel.name)
        assertEquals(user.id, channel.creatorId)
        assertEquals(Visibility.PUBLIC, channel.visibility)
    }

    @Test
    fun `Test get channels of user`() {
        val user1 = repoUsers.create("user1", "email@email.com", "password1", "token1")
        val user2 = repoUsers.create("user2", "email1@email.com", "password2", "token2")
        repoChannels.createChannel("channel1", user1.id, Visibility.PUBLIC)
        val channel2 = repoChannels.createChannel("channel2", user2.id, Visibility.PUBLIC)
        repoChannels.addUserToChannel(user1.id, channel2.id, Role.READ_WRITE)
        val channelsOfUser2 = repoChannels.getChannelsOfUser(user1.id)
        assertEquals(2, channelsOfUser2.size)
        assertEquals(listOf("channel1", "channel2"), channelsOfUser2.map { it.name })
    }

    @Test
    fun `Test get channel members`() {
        val user1 = repoUsers.create("user1", "email1@email.com", "password1", "token1")
        val user2 = repoUsers.create("user2", "emai2l@email.com", "password2", "token2")
        val channel1 = repoChannels.createChannel("channel1", user1.id, Visibility.PUBLIC)
        repoChannels.addUserToChannel(user1.id, channel1.id, Role.READ_WRITE)
        repoChannels.addUserToChannel(user2.id, channel1.id, Role.READ_WRITE)
        val members = repoChannels.getChannelMembers(channel1.id)
        assertEquals(listOf(user1.id, user2.id), members)
    }

    @Test
    fun `Test get channel by name`() {
        val user = repoUsers.create("user1", "email1@email.com", "password1", "token1")
        val channel = repoChannels.createChannel("channel1", user.id, Visibility.PUBLIC)
        val channelFound = repoChannels.getChannelByName("channel1")
        assertEquals(channel, channelFound)
    }

    @Test
    fun `Test find channel by id`() {
        val user = repoUsers.create("user1", "email1@email.com", "password1", "token1")
        val channel = repoChannels.createChannel("channel1", user.id, Visibility.PUBLIC)
        val channelFound = repoChannels.findById(channel.id)
        assertEquals(channel, channelFound)
    }

    @Test
    fun `Test add user to channel`() {
        val user = repoUsers.create("user1", "email1@email.com", "password1", "token1")
        val channel = repoChannels.createChannel("channel1", user.id, Visibility.PUBLIC)
        val channelWithUser = repoChannels.addUserToChannel(user.id, channel.id, Role.READ_WRITE)
        assertEquals(channel, channelWithUser)
    }






}