import kotlinx.datetime.Instant
import pt.isel.Sha256TokenEncoder
import pt.isel.Token
import pt.isel.TokenValidationInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenTests {

    @Test
    fun `should create a token`() {
        val token = Token(
            TokenValidationInfo("token"),
            1,
            Instant.DISTANT_PAST,
            Instant.DISTANT_FUTURE
        )
        assertEquals("token", token.token.validationInfo)
        assertEquals(1, token.userId)
        assertEquals(Instant.DISTANT_PAST, token.createdAt)
        assertEquals(Instant.DISTANT_FUTURE, token.lastUsedAt)
    }
}