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
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
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
            )
        assertIs<Success<RegisterInvitation>>(result)
        assertEquals(channel.value, result.value.channel)
        assertEquals(user.value, result.value.sender)
        assertEquals("alice@mail.com", result.value.email)
        assertEquals(Role.READ_WRITE, result.value.role)
    }

    @Test
    fun `getInvitationsOfUser should return the invitations of the user with the given id`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
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
            )
        assertIs<Success<ChannelInvitation>>(invitation)
        val invitations = invitationService.getInvitationsOfUser(user.value.id)
        assertIs<Success<List<Invitation>>>(invitations)
        assertEquals(invitation.value, invitations.value[0])
    }

    @Test
    fun `getInvitationsOfUser should return NegativeIdentifier error if userId is negative`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result = invitationService.getInvitationsOfUser(-1)
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.NegativeIdentifier, result.value)
    }

    @Test
    fun `getInvitationsOfUser should return UserNotFound error if userId does not exist`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result = invitationService.getInvitationsOfUser(9999)
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.UserNotFound, result.value)
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
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
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
            )
        assertIs<Success<RegisterInvitation>>(invitation)
        val result = invitationService.getRegisterInvitationById(invitation.value.id)
        assertIs<Success<Invitation>>(result)
    }

    @Test
    fun `acceptChannelInvitation should succeed`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
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
            )
        assertIs<Success<ChannelInvitation>>(channelInvitation)
        val result =
            invitationService.acceptChannelInvitation(channelInvitation.value.id)
        assertIs<Success<ChannelInvitation>>(result)
        val channelsOfUser2 = channelService.getChannelsOfUser(user2.value.id)
        assertIs<Success<List<Channel>>>(channelsOfUser2)
        assertEquals(channel.value, channelsOfUser2.value[1])
    }


    @Test
    fun `acceptChannelInvitation should return AlreadyUsed error if invitation was already used`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
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
            )
        assertIs<Success<ChannelInvitation>>(invitation)
        invitationService.acceptChannelInvitation(invitation.value.id)
        val result = invitationService.acceptChannelInvitation(invitation.value.id)
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.InvitationAlreadyUsed, result.value)
    }

    @Test
    fun `createRegisterInvitation with senderId negative`(){
        val user = userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createRegisterInvitation(
                -1,
                "alice@mail.com",
                channel.value.id,
                Role.READ_WRITE,
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.NegativeIdentifier, result.value)
    }

    @Test
    fun `createRegisterInvitation with email blank`() {
        val user = userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createRegisterInvitation(
                user.value.id,
                "",
                channel.value.id,
                Role.READ_WRITE,
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.InvalidEmail, result.value)
    }

    @Test
    fun `createRegisterInvitation with channelId negative`() {
        val user = userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createRegisterInvitation(
                user.value.id,
                "alice@mail.com",
                -1,
                Role.READ_WRITE,
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.NegativeIdentifier, result.value)
    }

    @Test
    fun `createRegisterInvitation with channel not found`() {
        val user = userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createRegisterInvitation(
                user.value.id,
                "alice@mail.com",
                16541,
                Role.READ_WRITE,
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.ChannelNotFound, result.value)
    }

    @Test
    fun `createRegisterInvitation with senderId unathorized`() {
        val user = userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser("user", "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createRegisterInvitation(
                315611,
                "alice@mail.com",
                channel.value.id,
                Role.READ_WRITE,
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.Unauthorized, result.value)
    }

    @Test
    fun `createChannelInvitation with senderId negative`() {
        val user = userService.addFirstUser("user", "bob@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser(user.value.username, "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createChannelInvitation(
                -1,
                user.value.id,
                channel.value.id,
                Role.READ_WRITE,
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.NegativeIdentifier, result.value)
    }

    @Test
    fun `createChannelInvitation with receiverId negative`() {
        val user = userService.addFirstUser("user", "email@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser(user.value.username, "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createChannelInvitation(
                user.value.id,
                -1,
                channel.value.id,
                Role.READ_WRITE,
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.NegativeIdentifier, result.value)
    }

    @Test
    fun `createChannelInvitation with channelId negative`() {
        val user = userService.addFirstUser("user", "email@email.co", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser(user.value.username, "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createChannelInvitation(
                user.value.id,
                user.value.id,
                -1,
                Role.READ_WRITE,
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.NegativeIdentifier, result.value)
    }
    /*
    @Test
    fun `createChannelInvitation with role invalid`() {
        val user = userService.addFirstUser("user", "email@mail.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser(user.value.username, "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createChannelInvitation(
                user.value.id,
                user.value.id,
                channel.value.id,
                Role.valueOf("INVALID"),
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.InvalidRole, result.value)
    }

     */

    @Test
    fun `createChannelInvitation with authenticatedUser unauthorized`() {
        val user = userService.addFirstUser("user", "email@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser(user.value.username, "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createChannelInvitation(
                315611,
                user.value.id,
                channel.value.id,
                Role.READ_WRITE,
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.Unauthorized, result.value)
    }

    @Test
    fun `createChannelInvitation with receiver invalid`() {
        val user = userService.addFirstUser("user", "emmail@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser(user.value.username, "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createChannelInvitation(
                user.value.id,
                315611,
                channel.value.id,
                Role.READ_WRITE,
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.InvalidReceiver, result.value)
    }

    @Test
    fun `createChannelInvitation with channel not found`() {
        val user = userService.addFirstUser("user", "email@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser(user.value.username, "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createChannelInvitation(
                user.value.id,
                user.value.id,
                315611,
                Role.READ_WRITE,
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.ChannelNotFound, result.value)
    }

    @Test
    fun `createChannelInvitation with authenticatedUser equal to receiver`() {
        val user = userService.addFirstUser("user", "email@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser(user.value.username, "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val result =
            invitationService.createChannelInvitation(
                user.value.id,
                user.value.id,
                channel.value.id,
                Role.READ_WRITE,
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.InvalidReceiver, result.value)
    }

    @Test
    fun `createChannelInvitation with channel visibility public`() {
        val user = userService.addFirstUser("user", "email@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val logged = userService.loginUser(user.value.username, "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val invitation = invitationService
            .createRegisterInvitation(user.value.id, "email2@email.com", channel.value.id, Role.READ_WRITE)
        assertIs<Success<RegisterInvitation>>(invitation)
        val user2 = userService.createUser("user2", invitation.value.email, "Strong_Password123", invitation.value.id)
        assertIs<Success<User>>(user2)
        val logged2 = userService.loginUser(user2.value.username, "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged2)
        val ch = channelService.createChannel("channel22", user2.value.id, Visibility.PUBLIC)
        assertIs<Success<Channel>>(ch)
        val result =
            invitationService.createChannelInvitation(
                user2.value.id,
                user.value.id,
                ch.value.id,
                Role.READ_WRITE,
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.CantInviteToPublicChannel, result.value)
    }

    @Test
    fun `createChannelInvitation with sender not belonging to channel`() {
        val user = userService.addFirstUser("user", "email@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val invitation = invitationService
            .createRegisterInvitation(user.value.id, "email2@email.com", channel.value.id, Role.READ_WRITE)
        assertIs<Success<RegisterInvitation>>(invitation)
        val user2 = userService.createUser("user2", invitation.value.email, "Strong_Password123", invitation.value.id)
        assertIs<Success<User>>(user2)
        val logged = userService.loginUser(user2.value.username, "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val ch2 = channelService.createChannel("channel432", user2.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(ch2)
        val result =
            invitationService.createChannelInvitation(
                user.value.id,
                user2.value.id,
                ch2.value.id,
                Role.READ_WRITE,
            )
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.SenderDoesntBelongToChannel, result.value)
    }

    @Test
    fun `createChannelInvitation with receiver already in channel`() {
        val user = userService.addFirstUser("user", "email@email.com", "Strong_Password123")
        assertIs<Success<User>>(user)
        val channel = channelService.createChannel("channel", user.value.id, Visibility.PRIVATE)
        assertIs<Success<Channel>>(channel)
        val invitation = invitationService
            .createRegisterInvitation(user.value.id, "mail2@email.com", channel.value.id, Role.READ_WRITE)
        assertIs<Success<RegisterInvitation>>(invitation)
        val user2 = userService.createUser("user2", invitation.value.email, "Strong_Password123", invitation.value.id)
        assertIs<Success<User>>(user2)
        val logged = userService.loginUser(user2.value.username, "Strong_Password123")
        assertIs<Success<AuthenticatedUser>>(logged)
        val invitation2 = invitationService
            .createChannelInvitation(user2.value.id, user.value.id, channel.value.id, Role.READ_WRITE)
        assertIs<Failure<InvitationError>>(invitation2)
        assertEquals(InvitationError.AlreadyInChannel, invitation2.value)
    }

    @Test
    fun `acceptChannelInvitation should return Invitation NotFound error `() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
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
        val result = invitationService.acceptChannelInvitation(165465)
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.InvitationNotFound, result.value)
    }

    @Test
    fun `test declineChannelInvitation`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
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
        val result =
            invitationService.createChannelInvitation(
                user.value.id,
                user2.value.id,
                channel.value.id,
                Role.READ_WRITE,
            )
        assertIs<Success<ChannelInvitation>>(result)
        val declined = invitationService.declineChannelInvitation(result.value.id)
        assertIs<Success<Unit>>(declined)
        val channelsOfUser2 = channelService.getChannelsOfUser(user2.value.id)
        assertIs<Success<List<Channel>>>(channelsOfUser2)
        assertEquals(1, channelsOfUser2.value.size)
    }
/*
    @Test
    fun `test declineChannelInvitation with invitation not found`() {
        val user =
            userService.addFirstUser("user", "bob@mail.com", "Strong_Password123")
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
        val result =
            invitationService.createChannelInvitation(
                user.value.id,
                user2.value.id,
                channel.value.id,
                Role.READ_WRITE,
            )
        assertIs<Success<ChannelInvitation>>(result)
        val declined = invitationService.declineChannelInvitation(165465)
        assertIs<Failure<InvitationError>>(declined)
        assertEquals(InvitationError.InvitationNotFound, declined.value)
    }

 */



}
