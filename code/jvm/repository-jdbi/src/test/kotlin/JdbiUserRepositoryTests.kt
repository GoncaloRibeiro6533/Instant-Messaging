import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.JdbiInvitationRepository
import pt.isel.JdbiSessionRepository
import pt.isel.JdbiUserRepository
import pt.isel.configureWithAppRequirements
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JdbiUserRepositoryTests {
    companion object {
        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()
    }

    @BeforeEach
    fun clean() {
        runWithHandle { handle: Handle ->
            JdbiUserRepository(handle).clear()
            JdbiSessionRepository(handle).clear()
            JdbiInvitationRepository(handle).clear()
        }
    }

    @Test
    fun `create user and find it`() {
        runWithHandle { handle: Handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@mail.com", "password")
            val foundUser = JdbiUserRepository(handle).findById(user.id)
            assertEquals(user, foundUser)
        }
    }

    @Test
    fun `find user by identifier`() {
        runWithHandle { handle: Handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@mail.com", "password")
            val foundUser = JdbiUserRepository(handle).findById(user.id)
            assertNotNull(foundUser)
            assertEquals(user, foundUser)
            assertEquals(user.id, foundUser.id)
        }
    }

    @Test
    fun `find user by username`() {
        runWithHandle { handle: Handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@mail.com", "password")
            val foundUser = JdbiUserRepository(handle).findByUsername("user", 1, 0)
            assertEquals(listOf(user), foundUser)
        }
    }

    @Test
    fun `update username`() {
        runWithHandle { handle: Handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@mail.com", "password")
            val updatedUser = JdbiUserRepository(handle).updateUsername(user, "newUsername")
            val foundUser = JdbiUserRepository(handle).findById(user.id)
            assertNotNull(foundUser)
            assertEquals(updatedUser, foundUser)
            assertEquals("newUsername", foundUser.username)
        }
    }

    @Test
    fun `delete user`() {
        runWithHandle { handle: Handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@mail.com", "password")
            assertNotNull(user)
            val deleted = JdbiUserRepository(handle).delete(user.id)
            val deletedUser = JdbiUserRepository(handle).findById(user.id)
            assertNull(deletedUser)
        }
    }

    @Test
    fun `find by email`() {
        runWithHandle { handle: Handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@mail.com", "password")
            val foundUser = JdbiUserRepository(handle).findByEmail("user@mail.com")
            assertNotNull(foundUser)
            assertEquals(user.email, foundUser.email)
            assertEquals(user, foundUser)
        }
    }

    @Test
    fun `find password of user`() {
        runWithHandle { handle: Handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@mail.com", "password")
            val password = JdbiUserRepository(handle).findPasswordOfUser(user)
            assertEquals("password", password)
        }
    }

    @Test
    fun `find all users`() {
        runWithHandle { handle: Handle ->
            val user1 = JdbiUserRepository(handle).createUser("username1", "user1@mail.com", "password1")
            val user2 = JdbiUserRepository(handle).createUser("username2", "user2@mail.com", "password2")
            val users = JdbiUserRepository(handle).findAll()
            assertEquals(listOf(user1, user2), users)
        }
    }
}
