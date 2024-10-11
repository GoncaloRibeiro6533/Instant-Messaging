import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MessageServiceTest {

    private lateinit var messageService: MessageService
    private lateinit var userService: UserService
    private lateinit var channelService: ChannelService


    @BeforeEach
    fun setUp() {
        val trxManager = TransactionManagerInMem()
        channelService = ChannelService(trxManager)
        messageService = MessageService(trxManager)
        userService = UserService(trxManager)

    }

    @Test
    fun `createMessage should succeed`() {
        val user = userService.createUser("user1", "email1@email.com", "password1")
        assertIs<Success<User>>(user)

        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", user.value.token)

        assertIs<Success<Message>>(result)
        assertEquals(user.value.id, result.value.senderId)
        assertEquals(channel.value.id, result.value.channelId)
        assertEquals("Hello, how are you?", result.value.content)

    }

    @Test
    fun `createMessage should return Unauthorized if token is invalid`() {
        val result = messageService.sendMessage(1, 1, "Hello, how are you?", "invalidToken")

        assertIs<Failure<MessageError.Unauthorized>>(result)
    }

    @Test
    fun `createMessage should return InvalidChannelId if channelId is invalid`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val result = messageService.sendMessage(-1, user.value.id, "Hello, how are you?", user.value.token)
        assertIs<Failure<MessageError.InvalidChannelId>>(result)
    }

    @Test
    fun `createMessage should return InvalidText if text is invalid`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result = messageService.sendMessage(channel.value.id, user.value.id, "", user.value.token)
        assertIs<Failure<MessageError.InvalidText>>(result)

    }

    @Test
    fun `createMessage should return InvalidUserId if userId is invalid`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result = messageService.sendMessage(channel.value.id, -1, "Hello, how are you?", user.value.token)

        assertIs<Failure<MessageError.InvalidUserId>>(result)

    }

    @Test
    fun `createMessage should return ChannelNotFound if channel does not exist`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val result = messageService.sendMessage(1, user.value.id, "Hello, how are you?", user.value.token)
        assertIs<Failure<MessageError.ChannelNotFound>>(result)

    }

    @Test
    fun `createMessage should return UserNotInChannel if user is not in channel`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val user2 = userService.createUser("user2", "email2@gmail.com", "password2")
        assertIs<Success<User>>(user2)

        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result =
            messageService.sendMessage(channel.value.id, user2.value.id, "Hello, how are you?", user2.value.token)
        assertIs<Failure<MessageError.UserNotInChannel>>(result)

    }

    @Test
    fun `findMessageById should succeed`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", user.value.token)
        assertIs<Success<Message>>(message)

        val result = messageService.findMessageById(message.value.id, user.value.token)
        assertIs<Success<Message?>>(result)
    }

    @Test
    fun `findMessageById should return Unauthorized if token is invalid`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", user.value.token)
        assertIs<Success<Message>>(message)

        val result = messageService.findMessageById(message.value.id, "Invalid token")
        assertIs<Failure<MessageError.Unauthorized>>(result)
    }

    @Test
    fun `findMessageById should return NegativeIdentifier if id is negative`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", user.value.token)
        assertIs<Success<Message>>(message)

        val result = messageService.findMessageById(-1, user.value.token)
        assertIs<Failure<MessageError.NegativeIdentifier>>(result)
    }

    @Test
    fun `findMessageById should return MessageNotFound if message does not exist`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val result = messageService.findMessageById(1, user.value.token)
        assertIs<Failure<MessageError.MessageNotFound>>(result)
    }

    @Test
    fun `findMessageById should return UserNotInChannel if user is not in channel`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val user2 = userService.createUser("user2", "email2@gmail.com", "password2")
        assertIs<Success<User>>(user2)

        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", user.value.token)
        assertIs<Success<Message>>(message)

        val result = messageService.findMessageById(message.value.id, user2.value.token)
        assertIs<Failure<MessageError.UserNotInChannel>>(result)
    }

    @Test
    fun `getMsgHistory should succeed`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message1 =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", user.value.token)
        assertIs<Success<Message>>(message1)

        val message2 =
            messageService.sendMessage(channel.value.id, user.value.id, "I'm fine, thank you!", user.value.token)
        assertIs<Success<Message>>(message2)

        val result = messageService.getMsgHistory(channel.value.id, 2, 0, user.value.token)

    }

    @Test
    fun `getMsgHistory should return Unauthorized if token is invalid`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message1 =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", user.value.token)
        assertIs<Success<Message>>(message1)

        val message2 =
            messageService.sendMessage(channel.value.id, user.value.id, "I'm fine, thank you!", user.value.token)
        assertIs<Success<Message>>(message2)

        val result = messageService.getMsgHistory(channel.value.id, 2, 0, "Invalid token")
        assertIs<Failure<MessageError.Unauthorized>>(result)

    }

    @Test
    fun `getMsgHistory should return InvalidChannelId if channelId is invalid`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val result = messageService.getMsgHistory(-1, 2, 0, user.value.token)
        assertIs<Failure<MessageError.InvalidChannelId>>(result)

    }

    @Test
    fun `getMsgHistory should return InvalidLimit if limit is invalid`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message1 =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", user.value.token)
        assertIs<Success<Message>>(message1)

        val message2 =
            messageService.sendMessage(channel.value.id, user.value.id, "I'm fine, thank you!", user.value.token)
        assertIs<Success<Message>>(message2)

        val result = messageService.getMsgHistory(channel.value.id, -1, 0, user.value.token)
        assertIs<Failure<MessageError.InvalidLimit>>(result)

    }

    @Test
    fun `getMsgHistory should return InvalidSkip if limit is invalid`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message1 =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", user.value.token)
        assertIs<Success<Message>>(message1)

        val message2 =
            messageService.sendMessage(channel.value.id, user.value.id, "I'm fine, thank you!", user.value.token)
        assertIs<Success<Message>>(message2)

        val result = messageService.getMsgHistory(channel.value.id, 2, -2, user.value.token)
        assertIs<Failure<MessageError.InvalidSkip>>(result)

    }

    @Test
    fun `getMsgHistory should return ChannelNotFound if channel does not exist`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val result = messageService.getMsgHistory(1, 2, 0, user.value.token)
        assertIs<Failure<MessageError.ChannelNotFound>>(result)

    }

    @Test
    fun `getMsgHistory should return UserNotInChannel if user is not in channel`() {
        val user = userService.createUser("user1", "email1@gmail.com", "password1")
        assertIs<Success<User>>(user)

        val user2 = userService.createUser("user2", "email2@gmail.com", "password2")
        assertIs<Success<User>>(user2)

        val channel = channelService.createChannel("channel1", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(channel)

        val message1 =
            messageService.sendMessage(channel.value.id, user.value.id, "Hello, how are you?", user.value.token)
        assertIs<Success<Message>>(message1)

        val message2 =
            messageService.sendMessage(channel.value.id, user.value.id, "I'm fine, thank you!", user.value.token)
        assertIs<Success<Message>>(message2)

        val result = messageService.getMsgHistory(channel.value.id, 2, 0, user2.value.token)
        assertIs<Failure<MessageError.UserNotInChannel>>(result)
    }
    
}