import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

import kotlin.test.Test

class ChannelTests {
    @Test
    fun `Channel creation succeeds`() {
        val channel = Channel(1, "channel1", 1, Visibility.PUBLIC)
        assertEquals(1, channel.id)
        assertEquals("channel1", channel.name)
        assertEquals(1, channel.creatorId)
        assertEquals(Visibility.PUBLIC, channel.visibility)
    }

    @Test
    fun `Channel creation with private visibility`() {
        val channel = Channel(2, "channel2", 2, Visibility.PRIVATE)
        assertEquals(2, channel.id)
        assertEquals("channel2", channel.name)
        assertEquals(2, channel.creatorId)
        assertEquals(Visibility.PRIVATE, channel.visibility)
    }

    @Test
    fun `Channel creation fails with blank name`() {
        assertThrows<IllegalArgumentException> {
            Channel(1, "", 1, Visibility.PUBLIC)
        }
    }

    @Test
    fun `Channel creation fails with id lower than 0`() {
        assertThrows<IllegalArgumentException> {
            Channel(-1, "channel1", 1, Visibility.PUBLIC)
        }
    }

    @Test
    fun `Channel creation fails with creatorId lower than 0`() {
        assertThrows<IllegalArgumentException> {
            Channel(1, "channel1", -1, Visibility.PUBLIC)
        }
    }

    @Test
    fun `Channel creation fails with invalid visibility`() {
        assertThrows<IllegalArgumentException> {
            Channel(1, "channel1", 1, Visibility.valueOf("INVALID-VISIBILITY"))
        }
    }


}