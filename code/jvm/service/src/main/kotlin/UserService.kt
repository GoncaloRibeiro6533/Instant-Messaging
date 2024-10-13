import jakarta.inject.Named
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

    data object NotFirstUser : UserError()

    data object InvalidEmail : UserError()

    data object EmailDoesNotMatchInvite : UserError()
}

@Named
class UserService(private val trxManager: TransactionManager) {
    fun addFirstUser(
        username: String,
        password: String,
        email: String,
    ) = trxManager.run {
        if (userRepo.findAll().isNotEmpty()) return@run failure(UserError.NotFirstUser)
        if (username.isBlank()) return@run failure(UserError.InvalidUsername)
        if (password.isBlank()) return@run failure(UserError.InvalidPassword)
        if (username.length > User.MAX_USERNAME_LENGTH) return@run failure(UserError.UsernameToLong)
        val token = UUID.randomUUID().toString()
        val user = userRepo.create(username, email, password.hashedWithSha256(), token)
        return@run success(user)
    }

    fun getUserById(
        id: Int,
        token: String,
    ): Either<UserError, User> =
        trxManager.run {
            userRepo.findByToken(token)
                ?: return@run failure(UserError.Unauthorized)
            if (id < 0) return@run failure(UserError.NegativeIdentifier)
            val user = userRepo.findById(id)
            return@run if (user != null) success(user) else failure(UserError.UserNotFound)
        }

    fun findUserByUsername(
        username: String,
        token: String,
    ): Either<UserError, List<User>> =
        trxManager.run {
            userRepo.findByToken(token)
                ?: return@run failure(UserError.Unauthorized)
            val users = userRepo.findByUsername(username, 10, 0)
            return@run success(users)
        }

    fun createUser(
        username: String,
        email: String,
        password: String,
        inviteId: Int,
    ): Either<UserError, User> =
        trxManager.run {
            if (inviteId < 0) return@run failure(UserError.NegativeIdentifier)
            val invitation =
                (invitationRepo.findRegisterInvitationById(inviteId) as? RegisterInvitation)
                    ?: return@run failure(UserError.InvalidInvite)
            if (invitation.isUsed) return@run failure(UserError.InvalidInvite)
            if (username.isBlank()) return@run failure(UserError.InvalidUsername)
            if (password.isBlank()) return@run failure(UserError.InvalidPassword)
            if (email.isBlank()) return@run failure(UserError.InvalidEmail)
            if (email != invitation.email) return@run failure(UserError.EmailDoesNotMatchInvite)
            if (username.length > User.MAX_USERNAME_LENGTH) return@run failure(UserError.UsernameToLong)
            if (userRepo.findByUsername(username, 1, 0).isNotEmpty()) {
                return@run failure(UserError.UsernameAlreadyExists)
            }
            val token = UUID.randomUUID().toString()
            val user = userRepo.create(username, email, password.hashedWithSha256(), token)
            invitationRepo.updateRegisterInvitation(invitation)
            val invitationChannel = invitation.channel
            val invitationRole = invitation.role
            if (invitationChannel != null && invitationRole != null) {
                channelRepo.addUserToChannel(user, invitationChannel, invitationRole)
            }
            return@run success(user)
        }

    fun loginUser(
        username: String,
        password: String,
    ): Either<UserError, User> =
        trxManager.run {
            if (username.isBlank()) return@run failure(UserError.InvalidUsername)
            if (password.isBlank()) return@run failure(UserError.InvalidPassword)
            userRepo.findByUsername(username, 1, 0).firstOrNull()
                ?: return@run failure(UserError.NoMatchingUsername)
            val userAuthenticated = userRepo.getByUsernameAndPassword(username, password.hashedWithSha256())
            return@run if (userAuthenticated != null) {
                success(userAuthenticated)
            } else {
                failure(UserError.NoMatchingPassword)
            }
        }

    fun updateUsername(
        token: String,
        newUsername: String,
    ): Either<UserError, User> =
        trxManager.run {
            val user =
                userRepo.findByToken(token)
                    ?: return@run failure(UserError.Unauthorized)
            if (newUsername.isBlank()) return@run failure(UserError.InvalidUsername)
            if (newUsername.length > User.MAX_USERNAME_LENGTH) return@run failure(UserError.UsernameToLong)
            if (userRepo.findByUsername(newUsername, 1, 0).isNotEmpty()) {
                return@run failure(UserError.UsernameAlreadyExists)
            }
            val userEdited = userRepo.updateUsername(token, newUsername)
            return@run success(userEdited)
        }

    fun deleteUser(
        id: Int,
        token: String,
    ): Either<UserError, User> =
        trxManager.run {
            userRepo.findByToken(token)
                ?: return@run failure(UserError.Unauthorized)
            if (id < 0) return@run failure(UserError.NegativeIdentifier)
            val userDeleted = userRepo.delete(id)
            return@run success(userDeleted)
        }

    fun clear() =
        trxManager.run {
            userRepo.clear()
        }

   /* private val random = SecureRandom()
    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun hashedWithSha256(password: String, salt: String) =
        MessageDigest.getInstance("SHA-256")
            .digest(password.encodeToByteArray() + salt.encodeToByteArray())
            .toHexString()*/
    @OptIn(ExperimentalStdlibApi::class)
    private fun String.hashedWithSha256() =
        MessageDigest.getInstance("SHA-256")
            .digest(this.encodeToByteArray())
            .toHexString()
}
