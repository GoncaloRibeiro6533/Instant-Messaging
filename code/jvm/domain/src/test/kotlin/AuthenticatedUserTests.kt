import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pt.isel.AuthenticatedUser
import pt.isel.User

class AuthenticatedUserTests {
    private val user =
        User(
            id = 1,
            username = "John Doe",
            email = "john@example.com",
        )

    @Test
    fun `should create an authenticated user`() {
        val result =
            AuthenticatedUser(
                user = user,
                token = "token",
            )
        assertEquals(user, result.user)
    }

    @Test
    fun `should throw an exception when token is empty`() {
        assertThrows<IllegalArgumentException> {
            AuthenticatedUser(
                user = user,
                token = "",
            )
        }
    }
}
