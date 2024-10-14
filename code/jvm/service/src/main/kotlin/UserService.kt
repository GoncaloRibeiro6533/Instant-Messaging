import jakarta.inject.Named

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

    data object NegativeSkip : UserError()

    data object NegativeLimit : UserError()

    data object SessionExpired : UserError()
}

@Named
class UserService(private val trxManager: TransactionManager) {
    private val sha256Token = Sha256Token()
    private val token = Token()

    fun addFirstUser(
        username: String,
        password: String,
        email: String,
    ) = trxManager.run {
        if (userRepo.findAll().isNotEmpty()) return@run failure(UserError.NotFirstUser)
        if (username.isBlank()) return@run failure(UserError.InvalidUsername)
        if (password.isBlank()) return@run failure(UserError.InvalidPassword)
        if (username.length > User.MAX_USERNAME_LENGTH) return@run failure(UserError.UsernameToLong)
        val user = userRepo.create(username, email, sha256Token.hashedWithSha256(password))
        return@run success(user)
    }

    fun logoutUser(token: String) =
        trxManager.run {
            val session = sessionRepo.findByToken(token) ?: return@run failure(UserError.Unauthorized)
            if (session.expired()) {
                sessionRepo.deleteSession(token)
                return@run failure(UserError.SessionExpired)
            }
            sessionRepo.deleteSession(token)
            return@run success(Unit)
        }

    fun getUserById(
        id: Int,
        token: String,
    ): Either<UserError, User> =
        trxManager.run {
            val session = sessionRepo.findByToken(token) ?: return@run failure(UserError.Unauthorized)
            if (session.expired()) {
                sessionRepo.deleteSession(token)
                return@run failure(UserError.SessionExpired)
            }
            if (id < 0) return@run failure(UserError.NegativeIdentifier)
            val user = userRepo.findById(id)
            return@run if (user != null) success(user) else failure(UserError.UserNotFound)
        }

    fun findUserByUsername(
        username: String,
        token: String,
        limit: Int = 10,
        skip: Int = 0,
    ): Either<UserError, List<User>> =
        trxManager.run {
            val session = sessionRepo.findByToken(token) ?: return@run failure(UserError.Unauthorized)
            if (session.expired()) {
                sessionRepo.deleteSession(token)
                return@run failure(UserError.SessionExpired)
            }
            if (limit < 0) return@run failure(UserError.NegativeLimit)
            if (skip < 0) return@run failure(UserError.NegativeSkip)
            val users = userRepo.findByUsername(username, limit, skip)
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
            val user = userRepo.create(username, email, sha256Token.hashedWithSha256(password))
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
    ): Either<UserError, AuthenticatedUser> =
        trxManager.run {
            if (username.isBlank()) return@run failure(UserError.InvalidUsername)
            if (password.isBlank()) return@run failure(UserError.InvalidPassword)
            userRepo.findByUsername(username, 1, 0).firstOrNull()
                ?: return@run failure(UserError.NoMatchingUsername)
            val userAuthenticated =
                userRepo.getByUsernameAndPassword(username, sha256Token.hashedWithSha256(password))
                    ?: return@run failure(UserError.NoMatchingPassword)
            val session = sessionRepo.createSession(userAuthenticated.id, token.generateToken())
            return@run success(AuthenticatedUser(userAuthenticated, session.token))
        }

    fun updateUsername(
        token: String,
        newUsername: String,
    ): Either<UserError, User> =
        trxManager.run {
            val session = sessionRepo.findByToken(token) ?: return@run failure(UserError.Unauthorized)
            if (session.expired()) {
                sessionRepo.deleteSession(token)
                return@run failure(UserError.SessionExpired)
            }
            if (newUsername.isBlank()) return@run failure(UserError.InvalidUsername)
            if (newUsername.length > User.MAX_USERNAME_LENGTH) return@run failure(UserError.UsernameToLong)
            if (userRepo.findByUsername(newUsername, 1, 0).isNotEmpty()) {
                return@run failure(UserError.UsernameAlreadyExists)
            }
            val userEdited = userRepo.updateUsername(session.userId, newUsername)
            return@run success(userEdited)
        }

    fun deleteUser(
        id: Int,
        token: String,
    ): Either<UserError, User> =
        trxManager.run {
            val session = sessionRepo.findByToken(token) ?: return@run failure(UserError.Unauthorized)
            if (session.expired()) {
                sessionRepo.deleteSession(token)
                return@run failure(UserError.SessionExpired)
            }
            if (id < 0) return@run failure(UserError.NegativeIdentifier)
            if (session.userId != id) return@run failure(UserError.Unauthorized)
            sessionRepo.findByUserId(id).forEach { sessionRepo.deleteSession(it.token) }
            val userDeleted = userRepo.delete(id)
            return@run success(userDeleted)
        }

    fun clear() =
        trxManager.run {
            userRepo.clear()
        }
}
