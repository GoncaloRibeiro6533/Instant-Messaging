@file:Suppress("ktlint")

package pt.isel.talkRooms

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.*
import pt.isel.controllers.ChannelController
import pt.isel.controllers.InvitationController
import pt.isel.controllers.UserController
import pt.isel.models.channel.*
import pt.isel.models.invitation.InvitationInputModelChannel
import pt.isel.models.invitation.InvitationInputModelRegister
import pt.isel.models.invitation.InvitationOutputModelChannel
import pt.isel.models.invitation.InvitationOutputModelRegister
import pt.isel.models.user.UserLoginCredentialsInput
import pt.isel.models.user.UserRegisterInput
import java.util.stream.Stream
import kotlin.test.assertNotNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class ChannelControllerTests {
    companion object {
        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()

        private val logger = LoggerFactory.getLogger(ChannelControllerTests::class.java)

        @JvmStatic
        fun transactionManagers(): Stream<TransactionManager> =
            Stream.of(
                TransactionManagerInMem().also {
                    cleanup(it)
                    logger.info("## Cleaned up InMem")
                                               },
                TransactionManagerJdbi(jdbi).also { cleanup(it)
                    logger.info("## Cleaned up JDBI")
                                                  },
            )

        private fun cleanup(trxManager: TransactionManager) {
            trxManager.run {
                invitationRepo.clear()
                messageRepo.clear()
                channelRepo.clear()
                sessionRepo.clear()
                userRepo.clear()
            }
        }

        private fun createUserService(
            trxManager: TransactionManager,
            testClock: TestClock,
            tokenTtl: Duration = 30.days,
            tokenRollingTtl: Duration = 30.minutes,
            maxTokensPerUser: Int = 3,
        ) = UserService(
            trxManager,
            UsersDomain(
                BCryptPasswordEncoder(),
                Sha256TokenEncoder(),
                UsersDomainConfig(
                    tokenSizeInBytes = 256 / 8,
                    tokenTtl = tokenTtl,
                    tokenRollingTtl,
                    maxTokensPerUser = maxTokensPerUser,
                ),
            ),
            testClock,
            TestEmitter(trxManager)
        )

        private fun createInvitationService(
            trxManager: TransactionManager,
            tokenTtl: Duration = 30.days,
            tokenRollingTtl: Duration = 30.minutes,
            maxTokensPerUser: Int = 3,
            emitter: UpdatesEmitter
        ) = InvitationService(
            trxManager,
            UsersDomain(
                BCryptPasswordEncoder(),
                Sha256TokenEncoder(),
                UsersDomainConfig(
                    tokenSizeInBytes = 256 / 8,
                    tokenTtl = tokenTtl,
                    tokenRollingTtl,
                    maxTokensPerUser = maxTokensPerUser,
                ),
            ),
            emitter = emitter,
            emailService = EmailServiceMock(),
        )

        private fun  createEmitters(trxManager: TransactionManager) =
            UpdatesEmitter(trxManager)

        private fun createInvitationController(trxManager: TransactionManager, emitter: UpdatesEmitter) =
            InvitationController(createInvitationService(trxManager, emitter = emitter))

        private fun createChannelController(trxManager: TransactionManager, emitter: UpdatesEmitter) =
            ChannelController(createChannelService(trxManager, emitter))

        private fun createChannelService(trxManager: TransactionManager, emitter: UpdatesEmitter) =
            ChannelService(trxManager,emitter)

        private fun createUserController(trxManager: TransactionManager) =
            UserController(createUserService(trxManager, TestClock()))
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create channel should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            )
        val user = channelCreator.body as User
        val header = channelCreator.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]

        val result =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user, cookie),
            )

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assert(result.body is ChannelOutputModel)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get channel by id should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            )
        val user = channelCreator.body as User
        val header = channelCreator.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]


        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as ChannelOutputModel

        val result = channelController.getChannelById(channel.id, AuthenticatedUser(user, cookie))
        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get channel by name should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            )
        val user = channelCreator.body as User
        val header = channelCreator.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]
        channelController.createChannel(
            CreateChannelInputModel(
                "channel1",
                Visibility.PRIVATE,
            ),
            AuthenticatedUser(user, cookie),
        ).body as ChannelOutputModel

        val result = channelController.getChannelByName("channel1", AuthenticatedUser(user, cookie), 10, 0)
        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get channel members should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)
        val invitationController = createInvitationController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            )
        val user = channelCreator.body as User
        val header = channelCreator.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as ChannelOutputModel

        val member1RegisterInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "member1@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as InvitationOutputModelRegister
        val code = trxManager.run {
            invitationRepo.findRegisterInvitationById(member1RegisterInvitation.id)?.code
        }
        assertNotNull(code)
        userController.register(
            UserRegisterInput(
                "member1",
                "member1@test.com",
                "Member_123dsad",
            ),
            code,
        ).body as User

        val result = channelController.getChannelMembers(channel.id, AuthenticatedUser(user, cookie))
        assertEquals(HttpStatus.OK, result.statusCode)

        val members = result.body as ChannelMembersList
        assertEquals(2, members.nMembers)
        assertEquals(Role.READ_WRITE, members.members.first { it.user.username == "member1" }.role)
        assertEquals(Role.READ_WRITE, members.members.first { it.user.username == "admin2" }.role)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get channel members of non existing channel should return error`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"))
        val user = channelCreator.body as User
        val header = channelCreator.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]
        val result = channelController.getChannelMembers(0, AuthenticatedUser(user, cookie))
        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get channels of user should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)
        val invitationController = createInvitationController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val user1 =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            )
        val user = user1.body as User
        val header = user1.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]

        val channel1 =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as ChannelOutputModel

        channelController.createChannel(
            CreateChannelInputModel(
                "channel2",
                Visibility.PRIVATE,
            ),
            AuthenticatedUser(user, cookie),
        ).body as ChannelOutputModel

        val member1RegisterInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "user2@test.com",
                    channel1.id,
                    Role.READ_WRITE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as InvitationOutputModelRegister
        val code = trxManager.run {
            invitationRepo.findRegisterInvitationById(member1RegisterInvitation.id)?.code
        }
        assertNotNull(code)
        userController.register(
            UserRegisterInput(
                "user2",
                "user2@test.com",
                "User_123dsad",
            ),
            code,
        ).body as User

        val user2 =
            userController.login(
                UserLoginCredentialsInput("user2", "User_123dsad"),
            )
        val user3 = user2.body as User
        val header1 = user2.headers["Set-Cookie"].toString()
        assertNotNull(header1)
        val cookie1 = header1.split(";")[0]

        val channel3 =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel3",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user3, cookie1),
            ).body as ChannelOutputModel

        val channelInvitation =
            invitationController.createChannelInvitation(
                InvitationInputModelChannel(
                        user.id,
                    channel3.id,
                    Role.READ_WRITE,
                ),
                AuthenticatedUser(user3, cookie1),
            ).body as InvitationOutputModelChannel

        invitationController.acceptChannelInvitation(
            channelInvitation.id,
            AuthenticatedUser(user, cookie),
        )

        val result = channelController.getChannelsOfUser(user.id,AuthenticatedUser(user, cookie))
        assertEquals(HttpStatus.OK, result.statusCode)

        val channels = result.body as ChannelOfUserList
        assertEquals(3, channels.nChannels)
        assertEquals(Role.READ_WRITE, channels.channels.first { it.channel.name == "channel1" }.role)
        assertEquals(Role.READ_WRITE, channels.channels.first { it.channel.name == "channel2" }.role)
        assertEquals(Role.READ_WRITE, channels.channels.first { it.channel.name == "channel3" }.role)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update channel's name should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val admin =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            )
        val user = admin.body as User
        val header = admin.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as ChannelOutputModel

        val result = channelController.updateChannelName(channel.id, "channel123", AuthenticatedUser(user, cookie))
        assertEquals(HttpStatus.OK, result.statusCode)

        val updatedChannel = channelController.getChannelById(channel.id, AuthenticatedUser(user, cookie)).body as ChannelOutputModel
        assertEquals("channel123", updatedChannel.name)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update name of non existing channel should return an error`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val admin =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            )
        val user = admin.body as User
        val header = admin.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]

        val result = channelController.updateChannelName(0, "channel123", AuthenticatedUser(user, cookie))
        assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update name of a channel where the user is not a member should return an error`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)
        val invitationController = createInvitationController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val admin =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            )
        val user = admin.body as User
        val header = admin.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as ChannelOutputModel

        val nonMemberRegisterInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "nonMember@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as InvitationOutputModelRegister
        val code = trxManager.run {
            invitationRepo.findRegisterInvitationById(nonMemberRegisterInvitation.id)?.code
        }
        assertNotNull(code)
        userController.register(
            UserRegisterInput(
                "nonMember",
                "nonMember@test.com",
                "NonMember_123dsad",
            ),
            code,
        ).body as User

        val nonMember =
            userController.login(
                UserLoginCredentialsInput("nonMember", "NonMember_123dsad"),
            )
        val user1 = nonMember.body as User
        val header1 = nonMember.headers["Set-Cookie"].toString()
        assertNotNull(header1)
        val cookie1 = header1.split(";")[0]

        channelController.leaveChannel(channel.id, AuthenticatedUser(user1, cookie1))

        val result = channelController.updateChannelName(channel.id, "channel123", AuthenticatedUser(user1, cookie1))
        assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `leave channel should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val admin =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            )
        val user = admin.body as User
        val header = admin.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as ChannelOutputModel

        val result = channelController.leaveChannel(channel.id, AuthenticatedUser(user, cookie))
        assertEquals(HttpStatus.OK, result.statusCode)
    }

    //TODO cant add another member to channel, not implemented
    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `add member to channel should succeed`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)
        val invitationController = createInvitationController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val admin =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            )
        val user = admin.body as User
        val header = admin.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]
        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as ChannelOutputModel

        val member1RegisterInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "member1@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as InvitationOutputModelRegister
        val code = trxManager.run {
            invitationRepo.findRegisterInvitationById(member1RegisterInvitation.id)?.code
        }
        assertNotNull(code)
        userController.register(
            UserRegisterInput(
                "member1",
                "member1@test.com",
                "Member_123dsad",
            ),
            code,
        ).body as User

        val member1 =
            userController.login(
                UserLoginCredentialsInput("member1", "Member_123dsad"),
            )
        val user1 = member1.body as User
        val header1 = member1.headers["Set-Cookie"].toString()
        assertNotNull(header1)
        val cookie1 = header1.split(";")[0]

        invitationController.createRegisterInvitation(
            InvitationInputModelRegister(
                "member2@test.com",
                channel.id,
                Role.READ_WRITE,
            ),
            AuthenticatedUser(user1, cookie1),
        )

        val channel2 =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel2",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as ChannelOutputModel

        val result =
            channelController.joinChannel(
                channel2.id,
                Role.READ_WRITE,
                AuthenticatedUser(user1, cookie1),
            )
        assertEquals(HttpStatus.OK, result.statusCode)

        val members: ChannelMembersList = channelController.getChannelMembers(channel2.id,
            AuthenticatedUser(user, cookie)).body as ChannelMembersList
        assertEquals(2, members.nMembers)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `add member to channel where the user is not in`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)
        val invitationController = createInvitationController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).body as User

        val admin =
            userController.login(
                UserLoginCredentialsInput("admin", "Admin_123dsad"),
            )
        val user = admin.body as User
        val header = admin.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]

        val channel =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as ChannelOutputModel

        val user2RegisterInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "user2@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as InvitationOutputModelRegister
        val code1 = trxManager.run {
            invitationRepo.findRegisterInvitationById(user2RegisterInvitation.id)?.code
        }
        assertNotNull(code1)
        userController.register(
            UserRegisterInput(
                "user2",
                "user2@test.com",
                "User2_123dsad",
            ),
            code1,
        ).body as User

        val user2 =
            userController.login(
                UserLoginCredentialsInput("user2", "User2_123dsad"),
            )
        val user1 = user2.body as User
        val header1 = user2.headers["Set-Cookie"].toString()
        assertNotNull(header1)
        val cookie1 = header1.split(";")[0]


        val user3RegisterInvitation =
            invitationController.createRegisterInvitation(
                InvitationInputModelRegister(
                    "user3@test.com",
                    channel.id,
                    Role.READ_WRITE,
                ),
                AuthenticatedUser(user, cookie),
            ).body as InvitationOutputModelRegister
        val code = trxManager.run {
            invitationRepo.findRegisterInvitationById(user3RegisterInvitation.id)?.code
        }
        assertNotNull(code)
        userController.register(
            UserRegisterInput(
                "user3",
                "user3@test.com",
                "User3_123dsad",
            ),
            code,
        ).body as User

        val user3 =
            userController.login(
                UserLoginCredentialsInput("user3", "User3_123dsad"),
            )
        val user4 = user3.body as User
        val header2 = user3.headers["Set-Cookie"].toString()
        assertNotNull(header2)
        val cookie2 = header2.split(";")[0]

        val channel2 =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel2",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user1, cookie1),
            ).body as ChannelOutputModel

        val result =
            channelController.joinChannel(
                channel2.id,
                Role.READ_WRITE,
                AuthenticatedUser(user4, cookie2),
            )

        assertEquals(HttpStatus.OK, result.statusCode)
    }

   /* @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `createChannel should fails`(trxManager: TransactionManager){
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            )
        val user = channelCreator.body as User
        val header = channelCreator.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]

        val result =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user, cookie),
            )

        assertEquals(HttpStatus.CREATED, result.statusCode)
        assert(result.body is ChannelOutputModel)
        val result2 =
            channelController.createChannel(
                CreateChannelInputModel(
                    "channel1",
                    Visibility.PRIVATE,
                ),
                AuthenticatedUser(user, cookie),
            )
        assertEquals(HttpStatus.CONFLICT, result2.statusCode)
    }*/

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `addUserToChannel should fail`(trxManager: TransactionManager){
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            )
        val user = channelCreator.body as User
        val header = channelCreator.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]
        val result = channelController.getChannelById(-1, AuthenticatedUser(user, cookie))
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getChannelById should fail`(trxManager: TransactionManager){
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            )
        val user = channelCreator.body as User
        val header = channelCreator.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]
        val result = channelController.getChannelById(-1, AuthenticatedUser(user, cookie))
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get channel by name should fail`(trxManager: TransactionManager) {
        val emitter = createEmitters(trxManager)
        val userController = createUserController(trxManager)
        val channelController = createChannelController(trxManager, emitter)

        userController.registerFirstUser(
            UserRegisterInput("admin2", "admin2@mail.com", "Admin_123dsad"),
        ).body as User

        val channelCreator =
            userController.login(
                UserLoginCredentialsInput("admin2", "Admin_123dsad"),
            )
        val user = channelCreator.body as User
        val header = channelCreator.headers["Set-Cookie"].toString()
        assertNotNull(header)
        val cookie = header.split(";")[0]

        channelController.createChannel(
            CreateChannelInputModel(
                "channel1",
                Visibility.PRIVATE,
            ),
            AuthenticatedUser(user, cookie),
        ).body as ChannelOutputModel

        val result = channelController.getChannelByName("channel2", AuthenticatedUser(user, cookie), -1, 0)
        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }


}
