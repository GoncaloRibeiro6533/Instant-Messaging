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
        userService = UserService(trxManager, UsersDomain())
        channelService = ChannelService(trxManager)
    }

    @Test
    fun `createRegisterInvitation should succeed`() {
        val user =
            userService.addFirstUser("user", "Strong_Password123", "bob@mail.com")
        assertIs<Success<User>>(user)
        val channel =
            channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createRegisterInvitation(
                user.value.id,
                "alice@mail.com",
                channel.value.id,
                Role.READ_WRITE,
                logged.value.token,
            )
        assertIs<Success<RegisterInvitation>>(result)
        assertEquals(channel.value, result.value.channel)
        assertEquals(user.value, result.value.sender)
        assertEquals("alice@mail.com", result.value.email)
        assertEquals(Role.READ_WRITE, result.value.role)
    }

    @Test
    fun `createRegisterInvitation should return Unauthorized if token is invalid`() {
        val result =
            invitationService
                .createRegisterInvitation(
                    1,
                    "bob@mail.com",
                    1,
                    Role.READ_WRITE,
                    "invalidToken",
                )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.Unauthorized, result.value)
    }

    @Test
    fun `getInvitationsOfUser should return the invitations of the user with the given id`() {
        val user =
            userService.addFirstUser("user", "Strong_Password123", "bob@mail.com")
        assertIs<Success<User>>(user)
        val channel =
            channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createRegisterInvitation(
                user.value.id,
                "alice@mail.com",
                channel.value.id,
                Role.READ_WRITE,
                logged.value.token,
            )
        assertIs<Success<RegisterInvitation>>(result)
        val user2 = userService.createUser("user2", "alice@mail.com", "Strong_Password123", result.value.id)
        assertIs<Success<User>>(user2)
        val logged2 = userService.loginUser("user2", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged2)
        val channel2 = channelService.createChannel("channel2", user2.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel2)
        val invitation =
            invitationService.createChannelInvitation(
                user2.value.id,
                user.value.id,
                channel2.value.id,
                Role.READ_WRITE,
                logged2.value.token,
            )
        assertIs<Success<ChannelInvitation>>(invitation)
        val invitations = invitationService.getInvitationsOfUser(user.value.id, logged.value.token)
        assertIs<Success<List<Invitation>>>(invitations)
        assertEquals(invitation.value, invitations.value[0])
    }

    @Test
    fun `getInvitationsOfUser should return Unauthorized if token is invalid`() {
        val result =
            invitationService.getInvitationsOfUser(1, "invalidToken")
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.Unauthorized, result.value)
    }

    @Test
    fun `getInvitationsOfUser should return NegativeIdentifier error if userId is negative`() {
        val user =
            userService.addFirstUser("user", "Strong_Password123", "bob@mail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result = invitationService.getInvitationsOfUser(-1, logged.value.token)
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.NegativeIdentifier, result.value)
    }

    @Test
    fun `getInvitationsOfUser should return Unauthorized error if userId does not exist`() {
        val user =
            userService.addFirstUser("user", "Strong_Password123", "bob@mail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result = invitationService.getInvitationsOfUser(2, logged.value.token)
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.Unauthorized, result.value)
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
            userService.addFirstUser("user", "Strong_Password123", "bob@mail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val invitation =
            invitationService.createRegisterInvitation(
                user.value.id,
                "alice@mail.com",
                channel.value.id,
                Role.READ_WRITE,
                logged.value.token,
            )
        assertIs<Success<RegisterInvitation>>(invitation)
        val result = invitationService.getRegisterInvitationById(invitation.value.id)
        assertIs<Success<Invitation>>(result)
    }

    @Test
    fun `acceptChannelInvitation should succeed`() {
        val user =
            userService.addFirstUser("user", "Strong_Password123", "bob@mail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val ch = channelService.createChannel("ch", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(ch)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                user.value.id,
                "alice@mail.com",
                ch.value.id,
                Role.READ_WRITE,
                logged.value.token,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)

        val user2 =
            userService
                .createUser("user2", "alice@mail.com", "Strong_Password123", registerInvitation.value.id)
        assertIs<Success<User>>(user2)
        val logged2 = userService.loginUser("user2", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged2)
        val channel =
            channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val channelInvitation =
            invitationService.createChannelInvitation(
                user.value.id,
                user2.value.id,
                channel.value.id,
                Role.READ_WRITE,
                logged.value.token,
            )
        assertIs<Success<ChannelInvitation>>(channelInvitation)
        val result =
            invitationService.acceptChannelInvitation(channelInvitation.value.id, logged2.value.token)
        assertIs<Success<ChannelInvitation>>(result)
        val channelsOfUser2 = channelService.getChannelsOfUser(user2.value.id)
        assertIs<Success<List<Channel>>>(channelsOfUser2)
        assertEquals(channel.value, channelsOfUser2.value[1])
    }

    @Test
    fun `acceptChannelInvitation should return Unauthorized if token is invalid`() {
        val result =
            invitationService.acceptChannelInvitation(1, "invalidToken")
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.Unauthorized, result.value)
    }

    @Test
    fun `acceptChannelInvitation should return AlreadyUsed error if invitation was already used`() {
        val user =
            userService.addFirstUser("user", "Strong_Password123", "bob@mail.com")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val ch = channelService.createChannel("ch", user.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(ch)
        val registerInvitation =
            invitationService.createRegisterInvitation(
                user.value.id,
                "alice@mail.com",
                ch.value.id,
                Role.READ_WRITE,
                logged.value.token,
            )
        assertIs<Success<RegisterInvitation>>(registerInvitation)
        val user2 =
            userService
                .createUser("user2", "alice@mail.com", "Strong_Password123", registerInvitation.value.id)
        assertIs<Success<User>>(user2)
        val logged2 = userService.loginUser("user2", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged2)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val invitation =
            invitationService.createChannelInvitation(
                user.value.id,
                user2.value.id,
                channel.value.id,
                Role.READ_WRITE,
                logged.value.token,
            )
        assertIs<Success<ChannelInvitation>>(invitation)
        invitationService.acceptChannelInvitation(invitation.value.id, logged2.value.token)
        val result = invitationService.acceptChannelInvitation(invitation.value.id, logged2.value.token)
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.InvitationAlreadyUsed, result.value)
    }
}
