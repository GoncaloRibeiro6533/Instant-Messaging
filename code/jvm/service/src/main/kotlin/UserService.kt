import java.security.MessageDigest
import java.util.UUID

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

    data object InvalidInvite : UserError()
}

class UserService(private val trxManager: TransactionManager) {
    fun getUserById(
        id: Int,
        token: String,
    ): Either<UserError, User> =
        trxManager.run {
            userRepo.findByToken(token)
                ?: return@run Either.Left(UserError.Unauthorized)
            if (id < 0) return@run Either.Left(UserError.NegativeIdentifier)
            val user = userRepo.findById(id)
            return@run if (user != null) Either.Right(user) else Either.Left(UserError.UserNotFound)
        }

    fun findUserByUsername(
        username: String,
        token: String,
    ): Either<UserError, List<User>> =
        trxManager.run {
            userRepo.findByToken(token)
                ?: return@run Either.Left(UserError.Unauthorized)
            val users = userRepo.findByUsername(username, 10, 0)
            return@run Either.Right(users)
        }

    fun createUser(
        username: String,
        email: String,
        password: String,
    ): Either<UserError, User> =
        trxManager.run {
            // if (inviteId < 0) return@run Either.Left(UserError.NegativeIdentifier) //TODO add inviteId
            // val invite = invitationRepo.findRegisterInvitationById(inviteId) ?: return@run Either.Left(UserError.InvalidInvite) //TODO test
            if (username.isBlank()) return@run Either.Left(UserError.InvalidUsername)
            if (password.isBlank()) return@run Either.Left(UserError.InvalidPassword)
            if (username.length > User.MAX_USERNAME_LENGTH) return@run Either.Left(UserError.UsernameToLong)
            if (userRepo.findByUsername(username, 1, 0).isNotEmpty()) {
                return@run Either.Left(UserError.UsernameAlreadyExists)
            }
            val token = UUID.randomUUID().toString()
            val user = userRepo.create(username, email, password.hashedWithSha256(), token)
            // invitationRepo.deleteRegisterInvitationById(inviteId)
            return@run Either.Right(user)
        }

    fun loginUser(
        username: String,
        password: String,
    ): Either<UserError, User> =
        trxManager.run {
            if (username.isBlank()) return@run Either.Left(UserError.InvalidUsername)
            if (password.isBlank()) return@run Either.Left(UserError.InvalidPassword)
            userRepo.findByUsername(username, 1, 0).firstOrNull()
                ?: return@run Either.Left(UserError.NoMatchingUsername)
            val userAuthenticated = userRepo.getByUsernameAndPassword(username, password.hashedWithSha256())
            return@run if (userAuthenticated != null) {
                Either.Right(userAuthenticated)
            } else {
                Either.Left(UserError.NoMatchingPassword)
            }
        }

    fun updateUsername(
        token: String,
        newUsername: String,
    ): Either<UserError, User> =
        trxManager.run {
            userRepo.findByToken(token)
                ?: return@run Either.Left(UserError.Unauthorized)
            if (newUsername.isBlank()) return@run Either.Left(UserError.InvalidUsername)
            if (newUsername.length > User.MAX_USERNAME_LENGTH) return@run Either.Left(UserError.UsernameToLong)
            if (userRepo.findByUsername(newUsername, 1, 0).isNotEmpty()) {
                return@run Either.Left(UserError.UsernameAlreadyExists)
            }
            val userEdited = userRepo.updateUsername(token, newUsername)
            return@run Either.Right(userEdited)
        }

    fun deleteUser(
        id: Int,
        token: String,
    ): Either<UserError, User> =
        trxManager.run {
            userRepo.findByToken(token)
                ?: return@run Either.Left(UserError.Unauthorized)
            if (id < 0) return@run Either.Left(UserError.NegativeIdentifier)
            val userDeleted = userRepo.delete(id)
            return@run Either.Right(userDeleted)
        }

    fun clear() =
        trxManager.run {
            userRepo.clear()
        }

    @OptIn(ExperimentalStdlibApi::class)
    private fun String.hashedWithSha256() =
        MessageDigest.getInstance("SHA-256")
            .digest(toByteArray())
            .toHexString()
}
