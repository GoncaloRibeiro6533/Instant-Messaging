import java.security.MessageDigest

class Sha256Token {
    @OptIn(ExperimentalStdlibApi::class)
    fun hashedWithSha256(token: String) =
        MessageDigest.getInstance("SHA-256")
            .digest(token.encodeToByteArray())
            .toHexString()
}
