import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MessageServiceTest {
    private lateinit var messageService: MessageService
    private lateinit var userService: UserService
    private lateinit var channelService: ChannelService
    private lateinit var invitationService: InvitationService

    @BeforeEach
    fun setUp() {
        val trxManager = TransactionManagerInMem()
        channelService = ChannelService(trxManager)
        messageService = MessageService(trxManager)
        userService = UserService(trxManager)
        invitationService = InvitationService(trxManager)
    }

    @Test
    fun `createMessage should succeed`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", logged.value.token)

        assertIs<Success<Message>>(result)
        assertEquals(user.value.id, result.value.sender.id)
        assertEquals(channel.value.id, result.value.channel.id)
        assertEquals("Hello, how are you?", result.value.content)
    }

    @Test
    fun `createMessage should return Unauthorized if token is invalid`() {
        val result = messageService.sendMessage(1, 1, "Hello, how are you?", "invalidToken")

        assertIs<Failure<MessageError.Unauthorized>>(result)
    }

    @Test
    fun `createMessage should return InvalidChannelId if channelId is invalid`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result = messageService.sendMessage(-1, user.value.id, "Hello, how are you?", logged.value.token)
        assertIs<Failure<MessageError.InvalidChannelId>>(result)
    }

    @Test
    fun `createMessage should return InvalidText if text is invalid`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result = messageService.sendMessage(channel.value.id, user.value.id, "", logged.value.token)
        assertIs<Failure<MessageError.InvalidText>>(result)
    }

    @Test
    fun `createMessage should return InvalidUserId if userId is invalid`() {
        val user =
            userService.addFirstUser("user", "password", "bob@mail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result = messageService.sendMessage(channel.value.id, -1, "Hello, how are you?", logged.value.token)

        assertIs<Failure<MessageError.InvalidUserId>>(result)
    }

    @Test
    fun `createMessage should return ChannelNotFound if channel does not exist`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result = messageService.sendMessage(1, user.value.id, "Hello, how are you?", logged.value.token)
        assertIs<Failure<MessageError.ChannelNotFound>>(result)
    }

    @Test
    fun `createMessage should return UserNotInChannel if user is not in channel`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                user.value.id,
                "alice@mail.com",
                null,
                null,
                logged.value.token,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)

        val user2 =
            userService
                .createUser("user2", "alice@mail.com", "password", registerInvitation.value.id)
        assertIs<Success<User>>(user2)
        val logged2 = userService.loginUser("user2", "password")
        assertIs<Success<AuthenticatedUser>>(logged2)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result =
            messageService.sendMessage(channel.value.id, user2.value.id, "Hello, how are you?", logged2.value.token)
        assertIs<Failure<MessageError.UserNotInChannel>>(result)
    }

    @Test
    fun `findMessageById should succeed`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", logged.value.token)
        assertIs<Success<Message>>(message)

        val result = messageService.findMessageById(message.value.id, logged.value.token)
        assertIs<Success<Message?>>(result)
    }

    @Test
    fun `findMessageById should return Unauthorized if token is invalid`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", logged.value.token)
        assertIs<Success<Message>>(message)

        val result = messageService.findMessageById(message.value.id, "Invalid token")
        assertIs<Failure<MessageError.Unauthorized>>(result)
    }

    @Test
    fun `findMessageById should return NegativeIdentifier if id is negative`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", logged.value.token)
        assertIs<Success<Message>>(message)

        val result = messageService.findMessageById(-1, logged.value.token)
        assertIs<Failure<MessageError.NegativeIdentifier>>(result)
    }

    @Test
    fun `findMessageById should return MessageNotFound if message does not exist`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result = messageService.findMessageById(1, logged.value.token)
        assertIs<Failure<MessageError.MessageNotFound>>(result)
    }

    @Test
    fun `findMessageById should return UserNotInChannel if user is not in channel`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                user.value.id,
                "alice@mail.com",
                null,
                null,
                logged.value.token,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)

        val user2 =
            userService
                .createUser("user2", "alice@mail.com", "password", registerInvitation.value.id)
        val logged2 = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged2)
        assertIs<Success<User>>(user2)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", logged.value.token)
        assertIs<Success<Message>>(message)

        val result = messageService.findMessageById(message.value.id, logged2.value.token)
        assertIs<Failure<MessageError.UserNotInChannel>>(result)
    }

    @Test
    fun `getMsgHistory should succeed`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message1 =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", logged.value.token)
        assertIs<Success<Message>>(message1)

        val message2 =
            messageService.sendMessage(channel.value.id, user.value.id, "I'm fine, thank you!", logged.value.token)
        assertIs<Success<Message>>(message2)

        messageService.getMsgHistory(channel.value.id, 2, 0, logged.value.token)
    }

    @Test
    fun `getMsgHistory should return Unauthorized if token is invalid`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message1 =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", logged.value.token)
        assertIs<Success<Message>>(message1)

        val message2 =
            messageService.sendMessage(channel.value.id, user.value.id, "I'm fine, thank you!", logged.value.token)
        assertIs<Success<Message>>(message2)

        val result = messageService.getMsgHistory(channel.value.id, 2, 0, "Invalid token")
        assertIs<Failure<MessageError.Unauthorized>>(result)
    }

    @Test
    fun `getMsgHistory should return InvalidChannelId if channelId is invalid`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result = messageService.getMsgHistory(-1, 2, 0, logged.value.token)
        assertIs<Failure<MessageError.InvalidChannelId>>(result)
    }

    @Test
    fun `getMsgHistory should return InvalidLimit if limit is invalid`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message1 =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", logged.value.token)
        assertIs<Success<Message>>(message1)

        val message2 =
            messageService.sendMessage(channel.value.id, user.value.id, "I'm fine, thank you!", logged.value.token)
        assertIs<Success<Message>>(message2)

        val result = messageService.getMsgHistory(channel.value.id, -1, 0, logged.value.token)
        assertIs<Failure<MessageError.InvalidLimit>>(result)
    }

    @Test
    fun `getMsgHistory should return InvalidSkip if limit is invalid`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message1 =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", logged.value.token)
        assertIs<Success<Message>>(message1)

        val message2 =
            messageService.sendMessage(channel.value.id, user.value.id, "I'm fine, thank you!", logged.value.token)
        assertIs<Success<Message>>(message2)

        val result = messageService.getMsgHistory(channel.value.id, 2, -2, logged.value.token)
        assertIs<Failure<MessageError.InvalidSkip>>(result)
    }

    @Test
    fun `getMsgHistory should return ChannelNotFound if channel does not exist`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result = messageService.getMsgHistory(1, 2, 0, logged.value.token)
        assertIs<Failure<MessageError.ChannelNotFound>>(result)
    }

    @Test
    fun `getMsgHistory should return UserNotInChannel if user is not in channel`() {
        val user = userService.addFirstUser("user1", "password1", "email1@gmail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                user.value.id,
                "alice@mail.com",
                null,
                null,
                logged.value.token,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val user2 =
            userService
                .createUser("user2", "alice@mail.com", "password", registerInvitation.value.id)
        assertIs<Success<User>>(user2)
        val logged2 = userService.loginUser("user1", "password")
        assertIs<Success<AuthenticatedUser>>(logged2)
        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message1 =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", logged.value.token)
        assertIs<Success<Message>>(message1)

        val message2 =
            messageService.sendMessage(channel.value.id, user.value.id, "I'm fine, thank you!", logged.value.token)
        assertIs<Success<Message>>(message2)

        val result = messageService.getMsgHistory(channel.value.id, 2, 0, logged2.value.token)
        assertIs<Failure<MessageError.UserNotInChannel>>(result)
    }
}
