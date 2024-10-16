import mocks.MockChannelRepository
import mocks.MockMessageRepo
import mocks.MockUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MessageRepoMockTests {
    private var user: User
    private var channel: Channel
    private val repoUsers =
        MockUserRepository().also {
            user =
                it.create(
                    "Bob",
                    "bob@mail.com",
                    "password",
                )
        }
    private val repoChannels =
        MockChannelRepository().also {
            channel = it.createChannel("channel", user, Visibility.PUBLIC)
            it.addUserToChannel(user, channel, Role.READ_WRITE)
        }
    private val repoMessages = MockMessageRepo()

    @Test
    fun `Test create message`() {
        val message = repoMessages.createMessage(user, channel, "message")
        assertEquals("message", message.content)
        assertEquals(user.id, message.sender.id)
        assertEquals(channel.id, message.channel.id)
    }

    @Test
    fun `Test find message by id`() {
        val message = repoMessages.createMessage(user, channel, "message")
        val messageFound = repoMessages.findById(message.id)
        assertEquals(message, messageFound)
    }

    @Test
    fun `Test find message by id with no message matching`() {
        val messageFound = repoMessages.findById(-1)
        assertEquals(null, messageFound)
    }

    @Test
    fun `Test find messages by channel`() {
        val message1 = repoMessages.createMessage(user, channel, "message1")
        val message2 = repoMessages.createMessage(user, channel, "message2")
        val messages = repoMessages.findByChannel(channel, 2, 0)
        assertEquals(listOf(message1, message2), messages)
    }

    @Test
    fun `Test delete message by id`() {
        val message = repoMessages.createMessage(user, channel, "message")
        val messageDeleted = repoMessages.deleteMessageById(message.id)
        assertEquals(message, messageDeleted)
    }

    @Test
    fun `Test delete message by id with one message`() {
        val message = repoMessages.createMessage(user, channel, "message")
        val messageDeleted = repoMessages.deleteMessageById(message.id)
        assertEquals(message, messageDeleted)
    }

    @Test
    fun `Test delete messages by channel`() {
        val message1 = repoMessages.createMessage(user, channel, "message1")
        val message2 = repoMessages.createMessage(user, channel, "message2")
        val messagesDeleted = repoMessages.deleteMessagesByChannel(channel.id)
        assertEquals(listOf(message1, message2), messagesDeleted)
    }

    @Test
    fun `Test clear`() {
        repoMessages.clear()
        assertEquals(0, repoMessages.findAll().size)
    }
}
