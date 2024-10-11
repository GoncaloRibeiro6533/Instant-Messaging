import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserTests {
    @Test
    fun `User creation succeeds`() {
        val user = User(1, "user1", "user1@mail.com", "token")
        assertEquals(1, user.id)
        assertEquals("user1", user.username)
        assertEquals("token", user.token)
    }

    @Test
    fun `User creation fails with blank username`() {
        assertThrows<IllegalArgumentException> {
            User(1, "", "user1@mail.com", "token")
        }
    }

    @Test
    fun `User creation fails with blank token`() {
        assertThrows<IllegalArgumentException> {
            User(1, "user1", "user1@mail.com", "")
        }
    }

    @Test
    fun `User creation fails with blank email`() {
        assertThrows<IllegalArgumentException> {
            User(1, "user1", "", "token")
        }
    }

    @Test
    fun `User creation fails with id lower than 0`() {
        assertThrows<IllegalArgumentException> {
            User(-1, "user1", "user1@mail.com", "token")
        }
    }

    @Test
    fun `User creation fails with username longer than 50 characters`() {
        assertThrows<IllegalArgumentException> {
            User(1, "user" + "1".repeat(User.MAX_USERNAME_LENGTH), "user1@mail.com", "token")
        }
    }
}
