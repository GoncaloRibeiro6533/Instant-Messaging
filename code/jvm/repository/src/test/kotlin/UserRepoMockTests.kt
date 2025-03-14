import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import pt.isel.User
import pt.isel.mocks.MockUserRepository

class UserRepoMockTests {
    private var user: User
    private val repoUsers =
        MockUserRepository().also {
            user = it.createUser("Bob", "bob@mail.com", "password")
        }

    @Test
    fun `Test create user`() {
        assertEquals("Bob", user.username)
        assertEquals("bob@mail.com", user.email)
    }

    @Test
    fun `Test find user by id`() {
        val userFound = repoUsers.findById(user.id)
        assertEquals(user, userFound)
    }

    @Test
    fun `Test find user by id with no user matching`() {
        val userFound = repoUsers.findById(-1)
        assertNull(userFound)
    }

    @Test
    fun `Test find all users`() {
        val users = repoUsers.findAll()
        assertEquals(listOf(user), users)
    }

    @Test
    fun `Test find user by username`() {
        val users = repoUsers.findByUsername(user.username, 1, 0)
        assertEquals(listOf(user), users)
    }

    @Test
    fun `Test update username`() {
        val user = repoUsers.createUser("Alice", "alice@mail.com", "password")
        val updatedUser = repoUsers.updateUsername(user, "Alice2")
        assertEquals("Alice2", updatedUser.username)
    }

    @Test
    fun `Test get user by username and password`() {
        val user = repoUsers.findByUsernameAndPassword("Bob", "password")
        assertEquals(user, user)
    }

    @Test
    fun `Test delete user`() {
        val user = repoUsers.createUser("John", "john@mail.com", "password")
        repoUsers.delete(user.id)
        assertEquals(null, repoUsers.findById(user.id))
    }

    @Test
    fun `Test find user by email`() {
        val user = repoUsers.findByEmail(user.email)
        assertEquals(user, user)
    }

    @Test
    fun `Test find user by email with no user matching`() {
        val user = repoUsers.findByEmail("notAnEmail")
        assertNull(user)
    }

    @Test
    fun `Test clear users`() {
        repoUsers.clear()
        assertEquals(emptyList<User>(), repoUsers.findAll())
    }

    @Test
    fun `Test find user by username and password with wrong password`() {
        val user = repoUsers.findByUsernameAndPassword("Bob", "password2")
        assertEquals(null, user)
    }

    @Test
    fun `Test find user by username and password with wrong username`() {
        val user = repoUsers.findByUsernameAndPassword("Bob2", "password")
        assertEquals(null, user)
    }

    @Test
    fun `find user by password with no user matching`() {
        val user = repoUsers.findByUsernameAndPassword("Bob2", "password2")
        assertEquals(null, user)
    }

    @Test
    fun `Test find user by username with no user matching`() {
        val users = repoUsers.findByUsername("Bob2", 1, 0)
        assertEquals(emptyList<User>(), users)
    }

    @Test
    fun `Test find user by username with  user matching`() {
        val userMatched = repoUsers.findUserMatchesUsername("Bob")
        assertEquals(user, userMatched)
    }

    @Test
    fun `find password of user`() {
        val password = repoUsers.findPasswordOfUser(user)
        assertEquals("password", password)
    }
}
