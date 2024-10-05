import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

import kotlin.test.Test

class ChannelTests {
    @Test
    fun `Channel creation succeeds`() {
        val channel = Channel(1, "channel1", 1, Visibility.PUBLIC, emptyList(), emptyMap())
        assertEquals(1, channel.id)
        assertEquals("channel1", channel.name)
        assertEquals(1, channel.creatorId)
        assertEquals(Visibility.PUBLIC, channel.visibility)
        assertEquals(emptyList<Message>(), channel.messages)
        assertEquals(emptyMap<User, Role>(), channel.users)
    }

    @Test
    fun `Channel creation fails with blank name`() {
        assertThrows<IllegalArgumentException> {
            Channel(1, "", 1, Visibility.PUBLIC, emptyList(), emptyMap())
        }
    }

    @Test
    fun `Channel creation fails with id lower than 0`() {
        assertThrows<IllegalArgumentException> {
            Channel(-1, "channel1", 1, Visibility.PUBLIC, emptyList(), emptyMap())
        }
    }

    @Test
    fun `Channel creation fails with creatorId lower than 0`() {
        assertThrows<IllegalArgumentException> {
            Channel(1, "channel1", -1, Visibility.PUBLIC, emptyList(), emptyMap())
        }
    }

    @Test
    fun `Channel creation fails with invalid visibility`() {
        assertThrows<IllegalArgumentException> {
            Channel(1, "channel1", 1, Visibility.valueOf("INVALID-VISIBILITY"), emptyList(), emptyMap())
        }
    }


}