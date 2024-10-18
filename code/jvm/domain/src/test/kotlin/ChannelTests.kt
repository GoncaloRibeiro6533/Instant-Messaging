import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import pt.isel.Channel
import pt.isel.User
import pt.isel.Visibility
import kotlin.test.Test

class ChannelTests {
    private val user = User(1, "username", "email@mail.com")

    @Test
    fun `Channel creation succeeds`() {
        val channel = Channel(1, "channel1", user, Visibility.PUBLIC)
        assertEquals(1, channel.id)
        assertEquals("channel1", channel.name)
        assertEquals(1, channel.creator.id)
        assertEquals(Visibility.PUBLIC, channel.visibility)
    }

    @Test
    fun `Channel creation with private visibility`() {
        val channel = Channel(2, "channel2", user, Visibility.PRIVATE)
        assertEquals(2, channel.id)
        assertEquals("channel2", channel.name)
        assertEquals(1, channel.creator.id)
        assertEquals(Visibility.PRIVATE, channel.visibility)
    }

    @Test
    fun `Channel creation fails with blank name`() {
        assertThrows<IllegalArgumentException> {
            Channel(1, "", user, Visibility.PUBLIC)
        }
    }

    @Test
    fun `Channel creation fails with id lower than 0`() {
        assertThrows<IllegalArgumentException> {
            Channel(-1, "channel1", user, Visibility.PUBLIC)
        }
    }

    @Test
    fun `Channel creation fails with invalid visibility`() {
        assertThrows<IllegalArgumentException> {
            Channel(1, "channel1", user, Visibility.valueOf("INVALID-VISIBILITY"))
        }
    }
}
