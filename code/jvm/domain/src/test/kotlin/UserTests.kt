import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pt.isel.User

class UserTests {
    @Test
    fun `User creation succeeds`() {
        val user = User(1, "user1", "user1@mail.com")
        assertEquals(1, user.id)
        assertEquals("user1", user.username)
    }

    @Test
    fun `User creation fails with blank username`() {
        assertThrows<IllegalArgumentException> {
            User(1, "", "user1@mail.com")
        }
    }

    @Test
    fun `User creation fails with blank email`() {
        assertThrows<IllegalArgumentException> {
            User(1, "user1", "")
        }
    }

    @Test
    fun `User creation fails with id lower than 0`() {
        assertThrows<IllegalArgumentException> {
            User(-1, "user1", "user1@mail.com")
        }
    }

    @Test
    fun `User creation fails with username longer than 50 characters`() {
        assertThrows<IllegalArgumentException> {
            User(1, "user" + "1".repeat(User.MAX_USERNAME_LENGTH), "user1@mail.com")
        }
    }
}
