import org.junit.jupiter.api.Test
import pt.isel.Channel
import pt.isel.ChannelInvitation
import pt.isel.RegisterInvitation
import pt.isel.Role
import pt.isel.User
import pt.isel.Visibility
import pt.isel.mocks.MockChannelRepository
import pt.isel.mocks.MockInvitationRepo
import pt.isel.mocks.MockUserRepository
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InvitationRepoMockTests {
    private var user: User
    private var user2: User
    private var registerInvitation: RegisterInvitation
    private var channelInvitation: ChannelInvitation
    private var channel: Channel
    private val repoUsers =
        MockUserRepository().also {
            user = it.createUser("Bob", "bob@mail.com", "password")
            user2 = it.createUser("John", "john@mail.com", "password")
        }
    private val repoChannels =
        MockChannelRepository().also {
            channel = it.createChannel("channel", user, Visibility.PUBLIC)
        }
    private val repoInvitations =
        MockInvitationRepo().also {
            registerInvitation = it.createRegisterInvitation(user, "some@mail.com", channel, Role.READ_WRITE)
            channelInvitation = it.createChannelInvitation(user, user2, channel, Role.READ_WRITE)
        }

    @Test
    fun `Test create register invitation`() {
        val invitation =
            repoInvitations.createRegisterInvitation(user, "alice@mail.com", channel, Role.READ_WRITE)
        assertEquals(user, invitation.sender)
        assertEquals("alice@mail.com", invitation.email)
        assertEquals(channel, invitation.channel)
        assertEquals(Role.READ_WRITE, invitation.role)
    }

    @Test
    fun `Test create channel invitation`() {
        val invitation =
            repoInvitations.createChannelInvitation(user, user2, channel, Role.READ_WRITE)
        assertEquals(user, invitation.sender)
        assertEquals(user2, invitation.receiver)
        assertEquals(channel, invitation.channel)
        assertEquals(Role.READ_WRITE, invitation.role)
    }

    @Test
    fun `Test find register invitation by id`() {
        val invitationFound = repoInvitations.findRegisterInvitationById(registerInvitation.id)
        assertEquals(registerInvitation, invitationFound)
    }

    @Test
    fun `Test find channel invitation by id`() {
        val invitationFound = repoInvitations.findChannelInvitationById(channelInvitation.id)
        assertEquals(channelInvitation, invitationFound)
    }

    @Test
    fun `Test update register invitation`() {
        val updatedInvitation = repoInvitations.updateRegisterInvitation(registerInvitation) as RegisterInvitation
        assertTrue(updatedInvitation.isUsed)
        assertEquals(registerInvitation.id, updatedInvitation.id)
        assertEquals(registerInvitation.sender, updatedInvitation.sender)
        assertEquals(registerInvitation.email, updatedInvitation.email)
        assertEquals(registerInvitation.channel, updatedInvitation.channel)
    }

    @Test
    fun `Test update channel invitation`() {
        val updatedInvitation = repoInvitations.updateChannelInvitation(channelInvitation) as ChannelInvitation
        assertEquals(channelInvitation.id, updatedInvitation.id)
        assertTrue(updatedInvitation.isUsed)
    }

    @Test
    fun `Test get invitations of user`() {
        val invitations = repoInvitations.getInvitationsOfUser(user2)
        assertEquals(listOf(channelInvitation), invitations)
    }

    @Test
    fun `Test delete register invitation by id`() {
        val deletedInvitation = repoInvitations.deleteRegisterInvitationById(registerInvitation.id)
        assertTrue(deletedInvitation)
        assertEquals(null, repoInvitations.findRegisterInvitationById(registerInvitation.id))
    }

    @Test
    fun `Test delete channel invitation by id`() {
        val deletedInvitation = repoInvitations.deleteChannelInvitationById(channelInvitation.id)
        assertTrue(deletedInvitation)
        assertEquals(null, repoInvitations.findChannelInvitationById(channelInvitation.id))
    }
}
