import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.Test

class MessageTests {
    @Test
    fun `should return message`() {
        val message = Message(1, 1, 1, "Hello, World!", LocalDateTime.now())
        assertEquals(1, message.id)
        assertEquals(1, message.senderId)
        assertEquals(1, message.channelId)
        assertEquals("Hello, World!", message.content)
        assertNotNull(message.timestamp)
    }

    @Test
    fun `should throw exception when id is lower than 0`() {
        assertThrows<IllegalArgumentException> {
            Message(-1, 1, 1, "Hello, World!", LocalDateTime.now())
        }
    }

    @Test
    fun `should throw exception when content is blank`() {
        assertThrows<IllegalArgumentException> {
            Message(1, 1, 1, "", LocalDateTime.now())
        }
    }

    @Test
    fun `should throw exception when content is longer than 1000 characters`() {
        assertThrows<IllegalArgumentException> {
            Message(1, 1, 1, "a".repeat(Message.MAX_MESSAGE_LENGTH + 1), LocalDateTime.now())
        }
    }

}