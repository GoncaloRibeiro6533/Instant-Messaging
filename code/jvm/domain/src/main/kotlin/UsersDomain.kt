import java.security.MessageDigest
import java.util.UUID

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
}
