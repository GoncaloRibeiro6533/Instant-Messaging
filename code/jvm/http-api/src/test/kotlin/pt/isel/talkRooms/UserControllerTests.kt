package pt.isel.talkRooms

import TransactionManager
import TransactionManagerInMem
import TransactionManagerJdbi
import UserService
import UsersDomain
import configureWithAppRequirements
import controllers.UserController
import models.user.UserRegisterInput
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.http.HttpStatus
import org.springframework.http.HttpHeaders
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class UserControllerTests {
    companion object{
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

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `add first user`(trxManager: TransactionManager) {
        val controllerUser = UserController(UserService(trxManager, UsersDomain()))

        // when: creating an user
        // then: the response is a 201 with a proper Location header
        val user = controllerUser.registerFirstUser(
            UserRegisterInput("admin", "admin@email.com", "Admin123dsad")
        ).let { resp ->
            assertEquals(HttpStatus.CREATED, resp.statusCode)
            val location = resp.headers.getFirst(HttpHeaders.LOCATION)
            assertNotNull(location)
            assertTrue(location.startsWith("/api/user/register"))
            location.split("/").last().toInt()
        }

    }

}
