import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import pt.isel.Channel
import pt.isel.ChannelInvitation
import pt.isel.RegisterInvitation
import pt.isel.Role
import pt.isel.User
import pt.isel.Visibility
import java.time.LocalDateTime
import kotlin.test.Test

class InvitationTests {
    private val user = User(1, "username", "email@mail.com")
    private val invitedUser = User(2, "invitedUsername", "invitedEmail@mail.com")
    private val channel = Channel(1, "channel", user, Visibility.PUBLIC)
    private val role = Role.READ_WRITE

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
                channel,
                role,
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
            RegisterInvitation(-1, user, invitedUser.email, channel, role, false, LocalDateTime.now())
        }
    }

    @Test
    fun `should throw exception when receiver email is empty in RegisterInvitatiton`() {
        assertThrows<IllegalArgumentException> {
            RegisterInvitation(1, user, "", channel, role, false, LocalDateTime.now())
        }
    }

    @Test
    fun `should throw exception when sender email is equal to receiver email`() {
        assertThrows<IllegalArgumentException> {
            RegisterInvitation(1, user, user.email, channel, role, false, LocalDateTime.now())
        }
    }

    @Test
    fun `Channel Invitation starts as not used`() {
        val invitation = ChannelInvitation(1, user, invitedUser, channel, Role.READ_WRITE, false, LocalDateTime.now())
        assertEquals(false, invitation.isUsed)
    }

    @Test
    fun `Register Invitation starts as not used`() {
        val invitation =
            RegisterInvitation(
                1,
                user,
                invitedUser.email,
                channel,
                role,
                false,
                LocalDateTime.now(),
            )
        assertEquals(false, invitation.isUsed)
    }

    @Test
    fun `Channel Invitation is marked as used`() {
        val invitation = ChannelInvitation(1, user, invitedUser, channel, Role.READ_WRITE, false, LocalDateTime.now())
        val markedInvitation = invitation.markAsUsed()
        assertEquals(true, markedInvitation.isUsed)
    }

    @Test
    fun `Register Invitation is marked as used`() {
        val invitation =
            RegisterInvitation(
                1,
                user,
                invitedUser.email,
                channel,
                role,
                false,
                LocalDateTime.now(),
            )
        val markedInvitation = invitation.markAsUsed()
        assertEquals(true, markedInvitation.isUsed)
    }

    @Test
    fun `Channel Invitation is equal to another`() {
        val time = LocalDateTime.now()
        val invitation = ChannelInvitation(1, user, invitedUser, channel, Role.READ_WRITE, false, time)
        val otherInvitation = ChannelInvitation(1, user, invitedUser, channel, Role.READ_WRITE, false, time)
        assertEquals(invitation, otherInvitation)
    }

    @Test
    fun `Register Invitation is equal to another`() {
        val time = LocalDateTime.now()
        val invitation =
            RegisterInvitation(
                1,
                user,
                invitedUser.email,
                channel,
                role,
                false,
                time,
            )
        val otherInvitation =
            RegisterInvitation(
                1,
                user,
                invitedUser.email,
                channel,
                role,
                false,
                time,
            )
        assertEquals(invitation, otherInvitation)
    }
}
