import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class InvitationServiceTests {


    private lateinit var invitationService: InvitationService
    private lateinit var userService: UserService
    private lateinit var channelService: ChannelService

    @BeforeEach
    fun setUp() {
        val trxManager = TransactionManagerInMem()
        invitationService = InvitationService(trxManager)
        userService = UserService(trxManager)
        channelService = ChannelService(trxManager)
    }


    @Test
    fun `createRegisterInvitation should succeed`() {
        val user =
            userService.createUser("user", "bob@mail.com", "password")
        assertIs<Success<User>>(user)
        val channel =
            channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val result =
            invitationService.createRegisterInvitation(user.value.id, "alice@mail.com",
                channel.value.id, "READ_WRITE", user.value.token)
        assertIs<Success<RegisterInvitation>>(result)
        assertEquals(channel.value, result.value.channel)
        assertEquals(user.value, result.value.sender)
        assertEquals("alice@mail.com", result.value.email)
        assertEquals(Role.READ_WRITE, result.value.role)
    }

    @Test
    fun `createRegisterInvitation should succeed with null channel`() {
        val user =
            userService.createUser("user", "bob@mail.com", "password")
        assertIs<Success<User>>(user)
        val result =
            invitationService.createRegisterInvitation(user.value.id, "alice@mail.com",
                null, null, user.value.token)
        assertIs<Success<RegisterInvitation>>(result)
        assertEquals(null, result.value.channel)
        assertEquals(user.value, result.value.sender)
        assertEquals("alice@mail.com", result.value.email)
        assertEquals(null, result.value.role)
    }


    @Test
    fun `createRegisterInvitation should return Unauthorized if token is invalid`() {
        val result =
            invitationService.createRegisterInvitation(1, "bob@mail.com", null, "READ_WRITE", "invalidToken")
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.Unauthorized, result.value)
    }

    @Test
    fun `getInvitationsOfUser should return Unauthorized if token is invalid`() {
        val result = invitationService.getInvitationsOfUser(1, "invalidToken")
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.Unauthorized, result.value)
    }

    @Test
    fun `getInvitationsOfUser should return NegativeIdentifier error if userId is negative`() {
        val user = userService.createUser("user", "email@mail.com", "password")
        assertIs<Success<User>>(user)
        val result = invitationService.getInvitationsOfUser(-1, user.value.token)
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.NegativeIdentifier, result.value)
    }

    @Test
    fun `getInvitationsOfUser should return UserNotFound error if userId does not exist`() {
        val user =
            userService.createUser("user", "bob@mail.com", "password")
        assertIs<Success<User>>(user)
        val result = invitationService.getInvitationsOfUser(2, user.value.token)
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.UserNotFound, result.value)
    }

    @Test
    fun `getInvitationsOfUser should return the list of register invitations of the user`() {
        val user =
            userService.createUser("user", "bob@mail.com", "password")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
       /* val invitation1 =
            invitationService.createRegisterInvitation(user.value.id, "alice@mail.com",
                channel.value.id, "READ_WRITE", user.value.token)
        assertIs<Success<RegisterInvitation>>(invitation1)*/
        val user2 =
            userService.createUser("user2", "alice@mail.com", "password")
        assertIs<Success<User>>(user2)
        val invitation =
            invitationService.createChannelInvitation(user.value.id, user2.value.id,
                channel.value.id, "READ_WRITE", user.value.token)
        assertIs<Success<ChannelInvitation>>(invitation)
        val result = invitationService.getInvitationsOfUser(user2.value.id, user2.value.token)
        assertIs<Success<List<Invitation>>>(result)
        assertEquals(invitation.value, result.value[0])
    }

    @Test
    fun `getRegisterInvitationById should return InvitationNotFound if invitation does not exist`() {
        val result = invitationService.getRegisterInvitationById(1)
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.InvitationNotFound, result.value)
    }

    @Test
    fun `getRegisterInvitationById should return the register invitation with the given id`() {
        val user =
            userService.createUser("user", "bob@mail.com" ,"password")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val invitation =
            invitationService.createRegisterInvitation(user.value.id, "alice@mail.com",
                channel.value.id, "READ_WRITE", user.value.token)
        assertIs<Success<RegisterInvitation>>(invitation)
        val result = invitationService.getRegisterInvitationById(invitation.value.id)
        assertIs<Success<Invitation>>(result)
    }

    @Test
    fun `acceptChannelInvitation should succeed`() {
        val user =
            userService.createUser("user", "bob@mail.com", "password")
        assertIs<Success<User>>(user)
        val user2 =
            userService.createUser("user2", "alice@mail.com", "password")
        assertIs<Success<User>>(user2)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val invitation =
            invitationService.createChannelInvitation(user.value.id, user2.value.id,
                channel.value.id, "READ_WRITE", user.value.token)
        assertIs<Success<ChannelInvitation>>(invitation)
        val result = invitationService.acceptChannelInvitation(invitation.value.id, user2.value.token)
        assertIs<Success<ChannelInvitation>>(result)
        val channelsOfUser2 = channelService.getChannelsOfUser(user2.value.id)
        assertIs<Success<List<Channel>>>(channelsOfUser2)
        assertEquals(channel.value, channelsOfUser2.value[0])
    }

    @Test
    fun `acceptChannelInvitation should return Unauthorized if token is invalid`() {
        val result = invitationService.acceptChannelInvitation(1, "invalidToken")
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.Unauthorized, result.value)
    }

    /*@Test
    fun `acceptChannelInvitation should return ChannelNotFound if channel does not exist anymore`() {
        val user =
            userService.createUser("user", "bob@mail.com", "password")
        assertIs<Success<User>>(user)
        val user2 =
            userService.createUser("user2", "alice@mail.com", "password")
        assertIs<Success<User>>(user2)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val invitation =
            invitationService.createChannelInvitation(user.value.id, user2.value.id,
                channel.value.id, "READ_WRITE", user.value.token)
        assertIs<Success<ChannelInvitation>>(invitation)
        channelService.deleteChannel(channel.value.id)
    }*/

}