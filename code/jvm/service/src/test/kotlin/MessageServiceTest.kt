import org.junit.jupiter.api.BeforeEach
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.AuthenticatedUser
import pt.isel.Channel
import pt.isel.ChannelService
import pt.isel.Failure
import pt.isel.InvitationService
import pt.isel.Message
import pt.isel.MessageError
import pt.isel.MessageService
import pt.isel.RegisterInvitation
import pt.isel.Role
import pt.isel.Sha256TokenEncoder
import pt.isel.Success
import pt.isel.TransactionManager
import pt.isel.TransactionManagerInMem
import pt.isel.User
import pt.isel.UserService
import pt.isel.UsersDomain
import pt.isel.UsersDomainConfig
import pt.isel.Visibility
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class MessageServiceTest {
    private lateinit var messageService: MessageService
    private lateinit var userService: UserService
    private lateinit var channelService: ChannelService
    private lateinit var invitationService: InvitationService

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

    @BeforeEach
    fun setUp() {
        val trxManager = TransactionManagerInMem()
        channelService = ChannelService(trxManager)
        messageService = MessageService(trxManager)
        userService = createUserService(trxManager, TestClock())
        invitationService = InvitationService(trxManager)
    }

    @Test
    fun `createMessage should succeed`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?")

        assertIs<Success<Message>>(result)
        assertEquals(user.value.id, result.value.sender.id)
        assertEquals(channel.value.id, result.value.channel.id)
        assertEquals("Hello, how are you?", result.value.content)
    }

    @Test
    fun `createMessage should return Unauthorized if token is invalid`() {
        val result = messageService.sendMessage(1, 1, "Hello, how are you?")

        assertIs<Failure<MessageError.Unauthorized>>(result)
    }

    @Test
    fun `createMessage should return InvalidChannelId if channelId is invalid`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result = messageService.sendMessage(-1, user.value.id, "Hello, how are you?")
        assertIs<Failure<MessageError.InvalidChannelId>>(result)
    }

    @Test
    fun `createMessage should return InvalidText if text is invalid`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result = messageService.sendMessage(channel.value.id, user.value.id, "")
        assertIs<Failure<MessageError.InvalidText>>(result)
    }

    @Test
    fun `createMessage should return InvalidUserId if userId is invalid`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result = messageService.sendMessage(channel.value.id, -1, "Hello, how are you?")

        assertIs<Failure<MessageError.InvalidUserId>>(result)
    }

    @Test
    fun `createMessage should return ChannelNotFound if channel does not exist`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result = messageService.sendMessage(1, user.value.id, "Hello, how are you?")
        assertIs<Failure<MessageError.ChannelNotFound>>(result)
    }

    @Test
    fun `createMessage should return UserNotInChannel if user is not in channel`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val ch = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(ch)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                user.value.id,
                "alice@mail.com",
                ch.value.id,
                Role.READ_WRITE,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)

        val user2 =
            userService
                .createUser("user2", "alice@mail.com", "Strong_Password123", registerInvitation.value.id)
        assertIs<Success<User>>(user2)
        val logged2 = userService.loginUser("user2", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged2)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result =
            messageService.sendMessage(channel.value.id, user2.value.id, "Hello, how are you?")
        assertIs<Failure<MessageError.UserNotInChannel>>(result)
    }

    @Test
    fun `findMessageById should succeed`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?")
        assertIs<Success<Message>>(message)

        val result = messageService.findMessageById(message.value.id, logged.value.user.id)
        assertIs<Success<Message?>>(result)
    }

    @Test
    fun `findMessageById should return NegativeIdentifier if id is negative`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?")
        assertIs<Success<Message>>(message)

        val result = messageService.findMessageById(-1, logged.value.user.id)
        assertIs<Failure<MessageError.NegativeIdentifier>>(result)
    }

    @Test
    fun `findMessageById should return MessageNotFound if message does not exist`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result = messageService.findMessageById(1, logged.value.user.id)
        assertIs<Failure<MessageError.MessageNotFound>>(result)
    }

    @Test
    fun `findMessageById should return UserNotInChannel if user is not in channel`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val ch = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(ch)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                user.value.id,
                "alice@mail.com",
                ch.value.id,
                Role.READ_WRITE,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)

        val user2 =
            userService
                .createUser("user2", "alice@mail.com", "Strong_Password123", registerInvitation.value.id)
        val logged2 = userService.loginUser("user2", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged2)
        assertIs<Success<User>>(user2)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?")
        assertIs<Success<Message>>(message)

        val result = messageService.findMessageById(message.value.id, logged2.value.user.id)
        assertIs<Failure<MessageError.UserNotInChannel>>(result)
    }

    @Test
    fun `getMsgHistory should succeed`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message1 =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?")
        assertIs<Success<Message>>(message1)

        val message2 =
            messageService.sendMessage(channel.value.id, user.value.id, "I'm fine, thank you!")
        assertIs<Success<Message>>(message2)

        messageService.getMsgHistory(channel.value.id, 2, 0, user.value.id)
    }

    @Test
    fun `getMsgHistory should return InvalidChannelId if channelId is invalid`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result = messageService.getMsgHistory(-1, 2, 0, user.value.id)
        assertIs<Failure<MessageError.InvalidChannelId>>(result)
    }

    @Test
    fun `getMsgHistory should return InvalidLimit if limit is invalid`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message1 =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?")
        assertIs<Success<Message>>(message1)

        val message2 =
            messageService.sendMessage(channel.value.id, user.value.id, "I'm fine, thank you!")
        assertIs<Success<Message>>(message2)

        val result = messageService.getMsgHistory(channel.value.id, -1, 0, user.value.id)
        assertIs<Failure<MessageError.InvalidLimit>>(result)
    }

    @Test
    fun `getMsgHistory should return InvalidSkip if limit is invalid`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message1 =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?")
        assertIs<Success<Message>>(message1)

        val message2 =
            messageService.sendMessage(channel.value.id, user.value.id, "I'm fine, thank you!")
        assertIs<Success<Message>>(message2)

        val result = messageService.getMsgHistory(channel.value.id, 2, -2, user.value.id)
        assertIs<Failure<MessageError.InvalidSkip>>(result)
    }

    @Test
    fun `getMsgHistory should return ChannelNotFound if channel does not exist`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result = messageService.getMsgHistory(1, 2, 0, user.value.id)
        assertIs<Failure<MessageError.ChannelNotFound>>(result)
    }

    @Test
    fun `getMsgHistory should return UserNotInChannel if user is not in channel`() {
        val user = userService.addFirstUser("user1", "email1@gmail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val ch = channelService.createChannel("channel", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(ch)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                user.value.id,
                "alice@mail.com",
                ch.value.id,
                Role.READ_WRITE,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val user2 =
            userService
                .createUser("user2", "alice@mail.com", "Strong_Password123", registerInvitation.value.id)
        assertIs<Success<User>>(user2)
        val logged2 = userService.loginUser("user2", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged2)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message1 =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?")
        assertIs<Success<Message>>(message1)

        val message2 =
            messageService.sendMessage(channel.value.id, user.value.id, "I'm fine, thank you!")
        assertIs<Success<Message>>(message2)

        val result = messageService.getMsgHistory(channel.value.id, 2, 0, user2.value.id)
        assertIs<Failure<MessageError.UserNotInChannel>>(result)
    }
}
