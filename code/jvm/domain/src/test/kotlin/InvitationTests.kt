import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.Test

class InvitationTests {
    private val user = User(1, "username", "email@mail.com")
    private val invitedUser = User(2, "invitedUsername", "invitedEmail@mail.com")
    private val channel = Channel(1, "channel", user, Visibility.PUBLIC)

    @Test
    fun `should create an Channelinvitation`() {
        val invitation = ChannelInvitation(1, user, invitedUser, channel, Role.READ_WRITE, false, LocalDateTime.now())
        assertEquals(1, invitation.id)
        assertEquals(user, invitation.sender)
        assertEquals(false, invitation.isUsed)
    }

    @Test
    fun `should create an Registerinvitation`() {
        val invitation =
            RegisterInvitation(
                1,
                user,
                invitedUser.email,
                null,
                null,
                false,
                LocalDateTime.now(),
            )
        assertEquals(1, invitation.id)
        assertEquals(user, invitation.sender)
        assertEquals(false, invitation.isUsed)
    }

    @Test
    fun `should throw exception when id is lower than 0 in ChannelInvitation`() {
        assertThrows<IllegalArgumentException> {
            ChannelInvitation(-1, user, invitedUser, channel, Role.READ_WRITE, false, LocalDateTime.now())
        }
    }

    @Test
    fun `should throw exception when id is lower than 0 in RegisterInvitation`() {
        assertThrows<IllegalArgumentException> {
            RegisterInvitation(-1, user, invitedUser.email, null, null, false, LocalDateTime.now())
        }
    }

    @Test
    fun `should throw exception when receiver email is empty in RegisterInvitatiton`() {
        assertThrows<IllegalArgumentException> {
            RegisterInvitation(1, user, "", null, null, false, LocalDateTime.now())
        }
    }

    @Test
    fun `should throw exception when sender email is equal to receiver email`() {
        assertThrows<IllegalArgumentException> {
            RegisterInvitation(1, user, user.email, null, null, false, LocalDateTime.now())
        }
    }
}
