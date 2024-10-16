import jakarta.inject.Named
import java.security.MessageDigest
import java.util.UUID

const val MIN_PASSWORD_LENGTH = 12

@Named
class UsersDomain {
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-z.-]+\\.[a-z]{2,4}$"
        return email.matches(emailRegex.toRegex())
    }

    fun generateToken(): String {
        return UUID.randomUUID().toString()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun hashedWithSha256(token: String) =
        MessageDigest.getInstance("SHA-256")
            .digest(token.encodeToByteArray())
            .toHexString()

    fun isPasswordStrong(password: String): Boolean {
        if (password.length < MIN_PASSWORD_LENGTH) return false
        if (!password.any { it.isDigit() }) return false
        if (!password.any { it.isLowerCase() }) return false
        if (!password.any { it.isUpperCase() }) return false
        if (password.all { it.isLetterOrDigit() }) return false
        return true
    }
}
