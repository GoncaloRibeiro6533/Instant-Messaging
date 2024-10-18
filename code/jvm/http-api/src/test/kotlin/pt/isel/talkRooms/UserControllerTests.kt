package pt.isel.talkRooms

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.Sha256TokenEncoder
import pt.isel.TransactionManager
import pt.isel.TransactionManagerInMem
import pt.isel.TransactionManagerJdbi
import pt.isel.UserService
import pt.isel.UsersDomain
import pt.isel.UsersDomainConfig
import pt.isel.configureWithAppRequirements
import pt.isel.controllers.UserController
import pt.isel.models.user.UserLoginCredentialsInput
import pt.isel.models.user.UserRegisterInput
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class UserControllerTests {
    companion object {
        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()

        @JvmStatic
        fun transactionManagers(): Stream<TransactionManager> =
            Stream.of(
                TransactionManagerInMem().also { cleanup(it) },
                TransactionManagerJdbi(jdbi).also { cleanup(it) },
            )

        private fun cleanup(trxManager: TransactionManager) {
            trxManager.run {
                messageRepo.clear()
                channelRepo.clear()
                invitationRepo.clear()
                sessionRepo.clear()
                userRepo.clear()
            }
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
    )

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `add first user`(trxManager: TransactionManager) {
        val controllerUser = UserController(createUserService(trxManager, TestClock()))

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        controllerUser.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "Admin_123dsad"),
        ).let { resp ->
            assertEquals(HttpStatus.CREATED, resp.statusCode)
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `add first user with invalid email`(trxManager: TransactionManager) {
        val controllerUser = UserController(createUserService(trxManager, TestClock()))

        // when: creating an user
        // then: the response is a 400 with a proper Location header
        controllerUser.registerFirstUser(
            UserRegisterInput("admin", "adminmail.com", "Admin_123dsad"),
        ).let { resp ->
            assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `add first user with invalid password`(trxManager: TransactionManager) {
        val controllerUser = UserController(createUserService(trxManager, TestClock()))

        // when: creating an user
        // then: the response is a 400 with a proper Location header
        controllerUser.registerFirstUser(
            UserRegisterInput("admin", "admin@mail.com", "1234"),
        )
            .let { resp ->
                assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
            }
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `register first user then login`(trxManager: TransactionManager) {
        val controllerUser = UserController(createUserService(trxManager, TestClock()))

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        controllerUser.registerFirstUser(
            UserRegisterInput("admin2", "admin123@mail.com", "Admin_123dsad"),
        ).let { resp ->
            assertEquals(HttpStatus.CREATED, resp.statusCode)
        }

        // when: login with the created user
        // then: the response is a 200 with a proper Location header
        controllerUser.login(
            UserLoginCredentialsInput("admin2", "Admin_123dsad"),
        ).let { resp ->
            assertEquals(HttpStatus.OK, resp.statusCode)
        }
    }
}
