import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import pt.isel.Token
import pt.isel.TokenValidationInfo
import kotlin.test.Test

class TokenTests {
    @Test
    fun `should create a token`() {
        val token = Token(TokenValidationInfo("token"), 1, Instant.DISTANT_PAST, Instant.DISTANT_PAST)
        assertEquals(1, token.userId)
        assertEquals(Instant.DISTANT_PAST, token.createdAt)
        assertEquals(Instant.DISTANT_PAST, token.lastUsedAt)
    }

    @Test
    fun `should throw exception when creating a token with invalid user id`() {
        try {
            Token(TokenValidationInfo("token"), -1, Instant.DISTANT_PAST, Instant.DISTANT_PAST)
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid user id", e.message)
        }
    }

    @Test
    fun `should throw exception when creating a token with invalid token validation info`() {
        try {
            Token(TokenValidationInfo(""), 1, Instant.DISTANT_PAST, Instant.DISTANT_PAST)
        } catch (e: IllegalArgumentException) {
            assertEquals("Invalid token validation info", e.message)
        }
    }
}
