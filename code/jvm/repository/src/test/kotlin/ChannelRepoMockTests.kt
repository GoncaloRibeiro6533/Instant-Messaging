import pt.isel.Role
import pt.isel.User
import pt.isel.Visibility
import pt.isel.mocks.MockChannelRepository
import pt.isel.mocks.MockUserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ChannelRepoMockTests {
    private var user: User
    private val repoUsers =
        MockUserRepository().also {
            user =
                it.createUser(
                    "Bob",
                    "bob@mail.com",
                    "password",
                )
        }
    private val repoChannels = MockChannelRepository()

    @Test
    fun `Test create channel`() {
        val channel = repoChannels.createChannel("channel", user, Visibility.PUBLIC)
        assertEquals("channel", channel.name)
        assertEquals(user.id, channel.creator.id)
        assertEquals(Visibility.PUBLIC, channel.visibility)
    }

    @Test
    fun `Test get channels of user`() {
        val channel1 = repoChannels.createChannel("channel1", user, Visibility.PUBLIC)
        val channel2 = repoChannels.createChannel("channel2", user, Visibility.PUBLIC)
        repoChannels.addUserToChannel(user, channel1, Role.READ_WRITE)
        repoChannels.addUserToChannel(user, channel2, Role.READ_WRITE)
        val channels = repoChannels.getChannelsOfUser(user)
        assertEquals(2, channels.size)
        assertEquals(listOf(channel1, channel2), channels)
    }

    @Test
    fun `Test get channel members`() {
        val channel1 = repoChannels.createChannel("channel3", user, Visibility.PUBLIC)
        val user1 = repoUsers.createUser("user1", "user1@mail.com", "password1")
        repoChannels.addUserToChannel(user, channel1, Role.READ_WRITE)
        repoChannels.addUserToChannel(user1, channel1, Role.READ_WRITE)
        val members = repoChannels.getChannelMembers(channel1)
        assertEquals(2, members.size)
        assertEquals(mapOf(user to Role.READ_WRITE, user1 to Role.READ_WRITE), members)
    }

    @Test
    fun `Test get channel by name`() {
        val channel = repoChannels.createChannel("channel4", user, Visibility.PUBLIC)
        val channelFound = repoChannels.getChannelByName("channel4", 1, 0).first()
        assertEquals(channel, channelFound)
    }

    @Test
    fun `Test find channel by id`() {
        val channel = repoChannels.createChannel("channel5", user, Visibility.PUBLIC)
        val channelFound = repoChannels.findById(channel.id)
        assertEquals(channel, channelFound)
    }

    @Test
    fun `Test add user to channel`() {
        val channel = repoChannels.createChannel("channel6", user, Visibility.PUBLIC)
        repoChannels.addUserToChannel(user, channel, Role.READ_WRITE)
        val members: Map<User, Role> = repoChannels.getChannelMembers(channel)
        assertEquals(1, members.size)
        assertNotNull(members[user])
        assertEquals(Role.READ_WRITE, members[user])
    }

    @Test
    fun `Test update channel name`() {
        val channel = repoChannels.createChannel("channel7", user, Visibility.PUBLIC)
        val updatedChannel = repoChannels.updateChannelName(channel, "channel7_updated")
        assertEquals("channel7_updated", updatedChannel.name)
    }

    @Test
    fun `Test leave channel`() {
        val channel = repoChannels.createChannel("channel8", user, Visibility.PUBLIC)
        repoChannels.addUserToChannel(user, channel, Role.READ_WRITE)
        repoChannels.leaveChannel(user, channel)
        val members = repoChannels.getChannelMembers(channel)
        assertEquals(0, members.size)
    }

    @Test
    fun `Test clear`() {
        val channel = repoChannels.createChannel("channel9", user, Visibility.PUBLIC)
        repoChannels.addUserToChannel(user, channel, Role.READ_WRITE)
        repoChannels.clear()
        val channels = repoChannels.getChannelsOfUser(user)
        assertEquals(0, channels.size)
    }
}
