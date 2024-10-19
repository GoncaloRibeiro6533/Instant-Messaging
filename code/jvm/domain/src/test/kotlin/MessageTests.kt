import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.assertThrows
import pt.isel.Channel
import pt.isel.Message
import pt.isel.User
import pt.isel.Visibility
import java.time.LocalDateTime
import kotlin.test.Test

class MessageTests {
    private val channel = Channel(1, "Aulas de DAW", User(1, "Ana", "ana@mail.com"), Visibility.PUBLIC)
    private val user = User(1, "Ana", "ana@mail.com")
    private val testClock = TestClock()

    @Test
    fun `should return message`() {
        val message = Message(1, user, channel, "Hello, World!", testClock.now())
        assertEquals(1, message.id)
        assertEquals(1, message.sender.id)
        assertEquals(1, message.channel.id)
        assertEquals("Hello, World!", message.content)
        assertNotNull(message.timestamp)
    }

    @Test
    fun `should throw exception when id is lower than 0`() {
        assertThrows<IllegalArgumentException> {
            Message(-1, user, channel, "Hello, World!", testClock.now())
        }
    }

    @Test
    fun `should throw exception when content is blank`() {
        assertThrows<IllegalArgumentException> {
            Message(1, user, channel, "", testClock.now())
        }
    }

    @Test
    fun `should throw exception when content is longer than 1000 characters`() {
        assertThrows<IllegalArgumentException> {
            Message(1, user, channel, "a".repeat(Message.MAX_MESSAGE_LENGTH + 1), testClock.now())
        }
    }
}
