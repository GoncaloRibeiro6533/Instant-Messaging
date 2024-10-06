import java.security.MessageDigest
import java.util.*

sealed class UserError {
    data object UserNotFound : UserError()
    data object InvalidUsername : UserError()
    data object InvalidPassword : UserError()
    data object UsernameAlreadyExists : UserError()
    data object NoMatchingUsername : UserError()
    data object NoMatchingPassword : UserError()
    data object Unauthorized : UserError()
    data object UsernameToLong : UserError()
    data object NegativeIdentifier : UserError()
}

class UserService(private val userRepository : UserRepository) {

    fun getUserById(id: Int, token: String) : Either<UserError, User> {
        userRepository.findUserByToken(token)
            ?: return Either.Left(UserError.Unauthorized)
        if (id < 0) return Either.Left(UserError.NegativeIdentifier)
        val user = userRepository.findUserById(id)
        return if (user != null) Either.Right(user) else Either.Left(UserError.UserNotFound)
    }

    fun findUserByUsername(username: String, token: String) : Either<UserError, List<User>> {
        userRepository.findUserByToken(token)
            ?: return Either.Left(UserError.Unauthorized)
        val users = userRepository.findUserByUsername(username, 10, 0)
        return Either.Right(users)
    }

    fun createUser(username: String, password: String) : Either<UserError, User> {
        if (username.isBlank()) return Either.Left(UserError.InvalidUsername)
        if (password.isBlank()) return Either.Left(UserError.InvalidPassword)
        if (username.length > User.MAX_USERNAME_LENGTH) return Either.Left(UserError.UsernameToLong)
        if (userRepository.findUserByUsername(username, 1, 0).isNotEmpty())
            return Either.Left(UserError.UsernameAlreadyExists)
        val token = UUID.randomUUID().toString()
        val user = userRepository.createUser(username, password.hashedWithSha256(), token)
        return Either.Right(user)
    }


    fun loginUser(username: String, password: String) : Either<UserError, User> {
        if (username.isBlank()) return Either.Left(UserError.InvalidUsername)
        if (password.isBlank()) return Either.Left(UserError.InvalidPassword)
        userRepository.findUserByUsername(username, 1, 0).firstOrNull()
            ?: return Either.Left(UserError.NoMatchingUsername)
        val userAuthenticated = userRepository.validateLogin(username, password.hashedWithSha256())
        return if (userAuthenticated!= null)
            Either.Right(userAuthenticated) else Either.Left(UserError.NoMatchingPassword)
    }

    fun updateUsername(token: String, newUsername: String) : Either<UserError, User> {
        userRepository.findUserByToken(token)
            ?: return Either.Left(UserError.Unauthorized)
        if (newUsername.isBlank()) return Either.Left(UserError.InvalidUsername)
        if (newUsername.length > User.MAX_USERNAME_LENGTH) return Either.Left(UserError.UsernameToLong)
        if (userRepository.findUserByUsername(newUsername, 1, 0).isNotEmpty())
            return Either.Left(UserError.UsernameAlreadyExists)
        val userEdited = userRepository.updateUsername(token, newUsername)
        return Either.Right(userEdited)
    }

    fun deleteUser(id: Int, token: String) : Either<UserError, User> {
        userRepository.findUserByToken(token)
            ?: return Either.Left(UserError.Unauthorized)
        if (id < 0) return Either.Left(UserError.NegativeIdentifier)
        val userDeleted = userRepository.deleteUser(id)
        return Either.Right(userDeleted)
    }

    fun clear() {
        userRepository.clear()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun String.hashedWithSha256() =
        MessageDigest.getInstance("SHA-256")
            .digest(toByteArray())
            .toHexString()
}


