package pt.isel.pipeline

import org.springframework.stereotype.Component
import pt.isel.AuthenticatedUser
import pt.isel.UserService

@Component
class RequestTokenProcessor(
    private val usersService: UserService,
) {
    fun processAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedUser? {
        if (authorizationValue == null) {
            return null
        }
        val parts = authorizationValue.trim().split(" ")
        if (parts.size != 1) {
            return null
        }
        if (parts[0].isEmpty()) {
            return null
        }
        return usersService.getUserByToken(parts[0])?.let {
            AuthenticatedUser(
                it,
                parts[0],
            )
        }
    }

    companion object {
        const val SCHEME = "bearer"
    }
}
