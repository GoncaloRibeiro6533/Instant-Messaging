import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.Test

class InvitationTests {
    @Test
    fun `should create an invitation`() {
        val invitation = Invitation(1, 1, 2, 1, false, LocalDateTime.now())
        assertEquals(1, invitation.id)
        assertEquals(1, invitation.senderId)
        assertEquals(2, invitation.receiverId)
        assertEquals(1, invitation.channelId)
        assertFalse(invitation.isUsed)
        assertNotNull(invitation.timestamp)
    }

    @Test
    fun `should throw exception when id is lower than 0`() {
        assertThrows<IllegalArgumentException> {
            Invitation(-1, 1, 2, 1, false, LocalDateTime.now())
        }
    }

    @Test
    fun `should throw exception when invitation is already used`() {
        assertThrows<IllegalArgumentException> {
            Invitation(1, 1, 2, 1, true, LocalDateTime.now())
        }
    }

    @Test
    fun `should throw exception when senderId is lower than 0`() {
        assertThrows<IllegalArgumentException> {
            Invitation(1, -1, 2, 1, false, LocalDateTime.now())
        }
    }

    @Test
    fun `should throw exception when receiverId is lower than 0`() {
        assertThrows<IllegalArgumentException> {
            Invitation(1, 1, -1, 1, false, LocalDateTime.now())
        }
    }

    @Test
    fun `should throw exception when channelId is lower than 0`() {
        assertThrows<IllegalArgumentException> {
            Invitation(1, 1, 2, -1, false, LocalDateTime.now())
        }
    }

    @Test
    fun `should throw exception when senderId is equal to receiverId`() {
        assertThrows<IllegalArgumentException> {
            Invitation(1, 1, 1, 1, false, LocalDateTime.now())
        }
    }
}