import java.util.UUID

class Token {
    fun generateToken(): String {
        return UUID.randomUUID().toString()
    }
}
