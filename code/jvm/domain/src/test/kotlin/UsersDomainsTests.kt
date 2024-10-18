import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UsersDomainsTests {
    @Test
    fun `isValidEmail should return true for valid email`() {
        val usersDomain = UsersDomain()
        val result = usersDomain.isValidEmail("bob@mail.com")
        assertTrue(result)
    }

    @Test
    fun `isValidEmail should return false for invalid email`() {
        val usersDomain = UsersDomain()
        val result = usersDomain.isValidEmail("bobmail.com")
        assertTrue(!result)
    }

    @Test
    fun `isPasswordStrong should return true for strong password`() {
        val usersDomain = UsersDomain()
        val result = usersDomain.isPasswordStrong("Strong_Password123")
        assertTrue(result)
    }

    @Test
    fun `isPasswordStrong should return false for weak password`() {
        val usersDomain = UsersDomain()
        val result = usersDomain.isPasswordStrong("weakpassword")
        assertTrue(!result)
    }

    @Test
    fun `isPasswordStrong should return false for password with no digits`() {
        val usersDomain = UsersDomain()
        val result = usersDomain.isPasswordStrong("Weakpassword")
        assertTrue(!result)
    }

    @Test
    fun `isPasswordStrong should return false for password with no lowercase letters`() {
        val usersDomain = UsersDomain()
        val result = usersDomain.isPasswordStrong("WEAKPASSWORD123")
        assertTrue(!result)
    }

    @Test
    fun `isPasswordStrong should return false for password with no uppercase letters`() {
        val usersDomain = UsersDomain()
        val result = usersDomain.isPasswordStrong("weakpassword123")
        assertTrue(!result)
    }

    @Test
    fun `isPasswordStrong should return false for password with only letters`() {
        val usersDomain = UsersDomain()
        val result = usersDomain.isPasswordStrong("Weakpassword")
        assertTrue(!result)
    }

    @Test
    fun `token should be generated`() {
        val usersDomain = UsersDomain()
        val result = usersDomain.generateTokenValue()
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `password should be hashed with SHA-256`() {
        val usersDomain = UsersDomain()
        val result =
            usersDomain
                .hashedWithSha256("Strong_Password123")
        assertTrue(result.isNotEmpty())
    }
}
