package pt.isel

import jakarta.inject.Named
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.springframework.security.crypto.password.PasswordEncoder
import java.security.SecureRandom
import java.util.Base64.getUrlDecoder
import java.util.Base64.getUrlEncoder

const val MIN_PASSWORD_LENGTH = 8

@Named
class UsersDomain(
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder,
    private val config: UsersDomainConfig,
) {
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-z.-]+\\.[a-z]{2,4}$"
        return email.matches(emailRegex.toRegex())
    }

    fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            getUrlEncoder().encodeToString(byteArray)
        }

    fun canBeToken(token: String): Boolean =
        try {
            getUrlDecoder().decode(token).size == config.tokenSizeInBytes
        } catch (ex: IllegalArgumentException) {
            false
        }

    fun validatePassword(
        password: String,
        validationInfo: PasswordValidationInfo,
    ) = passwordEncoder.matches(
        password,
        validationInfo.validationInfo,
    )

    fun createPasswordValidationInformation(password: String) =
        PasswordValidationInfo(
            validationInfo = passwordEncoder.encode(password),
        )

    fun isPasswordStrong(password: String): Boolean {
        if (password.length < MIN_PASSWORD_LENGTH) return false
        if (!password.any { it.isDigit() }) return false
        if (!password.any { it.isLowerCase() }) return false
        if (!password.any { it.isUpperCase() }) return false
        if (password.all { it.isLetterOrDigit() }) return false
        return true
    }

    fun getTokenExpiration(token: Token): Instant {
        val absoluteExpiration = token.createdAt + config.tokenTtl
        val rollingExpiration = token.lastUsedAt + config.tokenRollingTtl
        return if (absoluteExpiration < rollingExpiration) {
            absoluteExpiration
        } else {
            rollingExpiration
        }
    }

    fun createTokenValidationInformation(token: String) = tokenEncoder.createValidationInformation(token)

    fun isTokenTimeValid(
        clock: Clock,
        token: Token,
    ): Boolean {
        val now = clock.now()
        return token.createdAt <= now && (now - token.createdAt) <= config.tokenTtl && (now - token.lastUsedAt) <= config.tokenRollingTtl
    }

    val maxNumberOfTokensPerUser = config.maxTokensPerUser
}
