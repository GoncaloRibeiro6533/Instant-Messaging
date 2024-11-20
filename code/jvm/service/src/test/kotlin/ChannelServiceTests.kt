@file:Suppress("ktlint")
import org.junit.jupiter.api.BeforeEach
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class ChannelServiceTests {
    private lateinit var channelService: ChannelService
    private lateinit var userService: UserService
    private lateinit var invitationService: InvitationService

    private val testClock = TestClock()

    private fun createUserService(
        trxManager: TransactionManager,
        testClock: TestClock,
        tokenTtl: Duration = 30.days,
        tokenRollingTtl: Duration = 30.minutes,
        maxTokensPerUser: Int = 3,
    ) = UserService(
        trxManager,
        UsersDomain(
            BCryptPasswordEncoder(),
            Sha256TokenEncoder(),
            UsersDomainConfig(
                tokenSizeInBytes = 256 / 8,
                tokenTtl = tokenTtl,
                tokenRollingTtl,
                maxTokensPerUser = maxTokensPerUser,
            ),
        ),
        testClock,
    )

    private fun createInvitationService(
        trxManager: TransactionManager,
        tokenTtl: Duration = 30.days,
        tokenRollingTtl: Duration = 30.minutes,
        maxTokensPerUser: Int = 3,
    ) = InvitationService(
        trxManager,
        UsersDomain(
            BCryptPasswordEncoder(),
            Sha256TokenEncoder(),
            UsersDomainConfig(
                tokenSizeInBytes = 256 / 8,
                tokenTtl = tokenTtl,
                tokenRollingTtl,
                maxTokensPerUser = maxTokensPerUser,
            ),
        ),
    )

    @BeforeEach
    fun setUp() {
        val trxManager = TransactionManagerInMem()
        channelService = ChannelService(trxManager, ChEmitter(trxManager))
        userService = createUserService(trxManager, testClock)
        invitationService = createInvitationService(trxManager)
    }

    @Test
    fun `Test to get a channel ID`() {
        val user = userService.addFirstUser("user", "user@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val result = channelService.getChannelById(channel.value.id)
        assertIs<Success<Channel>>(result)
        assertEquals(channel.value.id, result.value.id)
        assertEquals(channel.value, result.value)
    }

    @Test
    fun `Test to get a channel by name`() {
        val user = userService.addFirstUser("user", "user@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val result = channelService.getChannelByName(user.value.id, channel.value.name)
        assertIs<Success<List<Channel>>>(result)
        assertEquals(channel.value.name, result.value.first().name)
        assertEquals(channel.value, result.value.first())
    }

    @Test
    fun `Test to create a channel`() {
        val user = userService.addFirstUser("user", "user@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val name = "channel2"
        val visibility = Visibility.PUBLIC
        val result = channelService.createChannel(name, user.value.id, visibility)
        assertIs<Success<Channel>>(result)
        assertEquals(name, result.value.name)
        assertEquals(visibility, result.value.visibility)
        assertEquals(user.value.id, result.value.creator.id)
    }

    @Test
    fun `Test to get channels of a user`() {
        val user = userService.addFirstUser("user", "user@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val result = channelService.getChannelsOfUser(user.value.id)
        assertIs<Success<List<Channel>>>(result)
        assertEquals(channel.value, result.value[0])
    }

    @Test
    fun `Test to get members of a channel`() {
        val user = userService.addFirstUser("user", "user@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val result = channelService.getChannelMembers(channel.value.id)
        assertIs<Success<List<User>>>(result)
        assertEquals(user.value, result.value[0])
        assertEquals(1, result.value.size)
    }

    @Test
    fun `Test to add users to a channel`() {
        val user =
            userService.addFirstUser("user", "user@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val invitation =
            invitationService.createRegisterInvitation(
                user.value.id,
                "bob@mail.com",
                channel.value.id,
                Role.READ_ONLY,
            )
        assertIs<Success<RegisterInvitation>>(invitation)
        val user2 = userService.createUser("user2", "bob@mail.com", "Strong_Password123", invitation.value.id)
        assertIs<Success<User>>(user2)
        val members = channelService.getChannelMembers(channel.value.id)
        assertIs<Success<List<User>>>(members)
        assertEquals(2, members.value.size)
        assertEquals(user.value, members.value[0])
        assertEquals(user2.value, members.value[1])
    }

    @Test
    fun `Test getChannelById with negative ID`() {
        val exception = channelService.getChannelById(-1)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.NegativeIdentifier, exception.value)
    }

    @Test
    fun `Test getChannelByName with blank name`() {
        val user =
            userService.addFirstUser("user", "user@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val exception = channelService.getChannelByName(user.value.id, "", 1, 0)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.InvalidChannelName, exception.value)
    }

    @Test
    fun `Test createChannel with blank name`() {
        val exception = channelService.createChannel("", 1, Visibility.PUBLIC)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.InvalidChannelName, exception.value)
    }

    @Test
    fun `Test getChannelsOfUser with negative user ID`() {
        val exception = channelService.getChannelsOfUser(-1)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.NegativeIdentifier, exception.value)
    }

    @Test
    fun `Test getChannelById with non-existent ID`() {
        val exception = channelService.getChannelById(999)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.ChannelNotFound, exception.value)
    }

    @Test
    fun `Test getChannelByName with non-existent name`() {
        val user =
            userService.addFirstUser("user", "user@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channels = channelService.getChannelByName(user.value.id, "non-existent", 1, 0)
        assertIs<Success<List<Channel>>>(channels)
        assertEquals(0, channels.value.size)
    }

    @Test
    fun `Test createChannel with existing name`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val exception = channelService.createChannel(channel.value.name, user.value.id, Visibility.PUBLIC)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.ChannelNameAlreadyExists, exception.value)
    }

    @Test
    fun `getChannelMembers should throw exception for negative channel ID`() {
        val exception = channelService.getChannelMembers(-1)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.NegativeIdentifier, exception.value)
    }

    @Test
    fun `getChannelMembers should throw exception for non-existent channel ID`() {
        val exception = channelService.getChannelMembers(999)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.ChannelNotFound, exception.value)
    }

    @Test
    fun `getChannelsOfUser should throw exception for non-existent user ID`() {
        val exception = channelService.getChannelsOfUser(999)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.UserNotFound, exception.value)
    }

    @Test
    fun `addUserToChannel should throw exception for negative user ID`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val exception = channelService.addUserToChannel(-1, channel.value.id, Role.READ_WRITE, user.value.id)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.NegativeIdentifier, exception.value)
    }

    @Test
    fun `addUserToChannel with negative Channel ID`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val exception = channelService.addUserToChannel(user.value.id, -1, Role.READ_WRITE, user.value.id)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.NegativeIdentifier, exception.value)
    }

    @Test
    fun `addUserToChannel with non-existent user ID`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val exception = channelService.addUserToChannel(999, channel.value.id, Role.READ_WRITE, user.value.id)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.UserNotFound, exception.value)
    }

    @Test
    fun `addUserToChannel with non-existent channel ID`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val exception = channelService.addUserToChannel(user.value.id, 999, Role.READ_WRITE, user.value.id)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.ChannelNotFound, exception.value)
    }

    @Test
    fun `getChannelsOfUser with a user without channels`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val result = channelService.getChannelsOfUser(user.value.id)
        assertIs<Success<List<Channel>>>(result)
        assertEquals(emptyList<Channel>(), result.value)
    }

    @Test
    fun `try to add a user to a channel that is already in it`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val exception = channelService.addUserToChannel(user.value.id, channel.value.id, Role.READ_WRITE, user.value.id)
        assertIs<Failure<ChannelError>>(exception)
        assertEquals(ChannelError.UserAlreadyInChannel, exception.value)
    }

/*
    @Test
    fun `Test createChannel with invalid visibility`() {
        val user = userService.createUser("user", "Strong_Password123", "email@email.com")
        assertIs<Success<User>>(user)
        val name = "channel2"
        val visibility = Visibility.valueOf("INVALID")
        val result = channelService.createChannel(name, user.value.id, visibility)
        assertIs<Failure<ChannelError>>(result)
        assertEquals(ChannelError.InvalidVisibility, result.value)
    }

 */

    @Test
    fun `Test to getChannelByName with invalid limit`() {
        val user = userService.addFirstUser("user", "user@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val result = channelService.getChannelByName(user.value.id, channel.value.name, -1, 0)
        assertIs<Failure<ChannelError>>(result)
        assertEquals(ChannelError.InvalidLimit, result.value)
    }

    @Test
    fun `Test to getChannelByName with invalid skip`() {
        val user = userService.addFirstUser("user", "user@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val result = channelService.getChannelByName(user.value.id, channel.value.name, 15, -1)
        assertIs<Failure<ChannelError>>(result)
        assertEquals(ChannelError.InvalidSkip, result.value)
    }

    @Test
    fun `Test to getChannelByName with invalid user ID`() {
        val user = userService.addFirstUser("user", "user@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val result = channelService.getChannelByName(165, "channel", 10, 0)
        assertIs<Failure<ChannelError>>(result)
        assertEquals(ChannelError.UserNotFound, result.value)
    }

    @Test
    fun `non existent user tries to create a channel`() {
        val result = channelService.createChannel("channel", 999, Visibility.PUBLIC)
        assertIs<Failure<ChannelError>>(result)
        assertEquals(ChannelError.UserNotFound, result.value)
    }

    @Test
    fun `update a channel name`() {
        val user = userService.addFirstUser("user", "user@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val result = channelService.updateChannelName(channel.value.id, "new_channelName", user.value.id)
        assertIs<Success<Channel>>(result)
        assertEquals("new_channelName", result.value.name)
    }

    @Test
    fun `update a channel with negative channel ID`() {
        val user = userService.addFirstUser("user", "user@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val result = channelService.updateChannelName(-1, "new_channelName", user.value.id)
        assertIs<Failure<ChannelError>>(result)
        assertEquals(ChannelError.NegativeIdentifier, result.value)
    }

    @Test
    fun `update a channel with non-existent channel ID`() {
        val user = userService.addFirstUser("user", "user@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val result = channelService.updateChannelName(999, "new_channelName", user.value.id)
        assertIs<Failure<ChannelError>>(result)
        assertEquals(ChannelError.ChannelNotFound, result.value)
    }

    @Test
    fun `update a channel with blank name`() {
        val user = userService.addFirstUser("user", "user@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val result = channelService.updateChannelName(channel.value.id, "", user.value.id)
        assertIs<Failure<ChannelError>>(result)
        assertEquals(ChannelError.InvalidChannelName, result.value)
    }

    @Test
    fun `update a channel with existing name`() {
        val user = userService.addFirstUser("user", "user@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val channel2 = channelService.createChannel("channel2", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel2)
        val result = channelService.updateChannelName(channel.value.id, channel2.value.name, user.value.id)
        assertIs<Failure<ChannelError>>(result)
        assertEquals(ChannelError.ChannelNameAlreadyExists, result.value)
    }

    @Test
    fun `leave channel`() {
        val user = userService.addFirstUser("user", "user@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val leave = channelService.leaveChannel(user.value.id, channel.value.id)
        assertIs<Success<Channel>>(leave)
        val members = channelService.getChannelMembers(channel.value.id)
        assertIs<Success<List<User>>>(members)
        assertEquals(0, members.value.size)
    }

    @Test
    fun `leave channel with negative user ID`() {
        val user = userService.addFirstUser("user", "email@emiail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val leave = channelService.leaveChannel(-1, channel.value.id)
        assertIs<Failure<ChannelError>>(leave)
        assertEquals(ChannelError.NegativeIdentifier, leave.value)
    }

    @Test
    fun `leave channel with negative channel ID`() {
        val user = userService.addFirstUser("user", "email@emaio.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val leave = channelService.leaveChannel(user.value.id, -1)
        assertIs<Failure<ChannelError>>(leave)
        assertEquals(ChannelError.NegativeIdentifier, leave.value)
    }

    @Test
    fun `leave channel with non-existent user ID`() {
        val user = userService.addFirstUser("user", "email@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)
        val leave = channelService.leaveChannel(999, channel.value.id)
        assertIs<Failure<ChannelError>>(leave)
        assertEquals(ChannelError.UserNotFound, leave.value)
    }

    @Test
    fun `leave a non existent channel`() {
        val user = userService.addFirstUser("user", "email@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val leave = channelService.leaveChannel(user.value.id, 999)
        assertIs<Failure<ChannelError>>(leave)
        assertEquals(ChannelError.ChannelNotFound, leave.value)
    }
}
