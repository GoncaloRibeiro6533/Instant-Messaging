import jakarta.inject.Named

sealed class UserError {
    data object UserNotFound : UserError()

    data object UsernameAlreadyExists : UserError()

    data object NoMatchingUsername : UserError()

    data object NoMatchingPassword : UserError()

    data object Unauthorized : UserError()

    data object UsernameToLong : UserError()

    data object NegativeIdentifier : UserError()

    data object NotFirstUser : UserError()

    data object InvalidEmail : UserError()

    data object EmailDoesNotMatchInvite : UserError()

    data object NegativeSkip : UserError()

    data object NegativeLimit : UserError()

    data object SessionExpired : UserError()

    data object InvitationNotFound : UserError()

    data object InvitationAlreadyUsed : UserError()

    data object EmailCannotBeBlank : UserError()

    data object UsernameCannotBeBlank : UserError()

    data object PasswordCannotBeBlank : UserError()

    data object EmailAlreadyInUse : UserError()

    data object WeakPassword : UserError()
}

@Named
class UserService(private val trxManager: TransactionManager, private val usersDomain: UsersDomain) {
    fun addFirstUser(
        username: String,
        password: String,
        email: String,
    ) = trxManager.run {
        if (userRepo.findAll().isNotEmpty()) return@run failure(UserError.NotFirstUser)
        if (username.isBlank()) return@run failure(UserError.UsernameCannotBeBlank)
        if (password.isBlank()) return@run failure(UserError.PasswordCannotBeBlank)
        if (username.length > User.MAX_USERNAME_LENGTH) return@run failure(UserError.UsernameToLong)
        if (email.isBlank()) return@run failure(UserError.EmailCannotBeBlank)
        if (!usersDomain.isValidEmail(email)) return@run failure(UserError.InvalidEmail)
        if (!usersDomain.isPasswordStrong(password)) return@run failure(UserError.WeakPassword)
        val user = userRepo.create(username, email, usersDomain.hashedWithSha256(password))
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
            val user = userRepo.findById(id) ?: return@run failure(UserError.UserNotFound)
            return@run success(user)
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
            if (username.isBlank()) return@run failure(UserError.UsernameCannotBeBlank)
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
                    ?: return@run failure(UserError.InvitationNotFound)
            if (invitation.isUsed) return@run failure(UserError.InvitationAlreadyUsed)
            if (username.isBlank()) return@run failure(UserError.UsernameCannotBeBlank)
            if (password.isBlank()) return@run failure(UserError.PasswordCannotBeBlank)
            if (email.isBlank()) return@run failure(UserError.EmailCannotBeBlank)
            if (!usersDomain.isValidEmail(email)) return@run failure(UserError.InvalidEmail)
            if (userRepo.findByEmail(email) != null) return@run failure(UserError.EmailAlreadyInUse)
            if (email != invitation.email) return@run failure(UserError.EmailDoesNotMatchInvite)
            if (username.length > User.MAX_USERNAME_LENGTH) return@run failure(UserError.UsernameToLong)
            if (userRepo.findByUsername(username, 1, 0).isNotEmpty()) {
                return@run failure(UserError.UsernameAlreadyExists)
            }
            if (!usersDomain.isPasswordStrong(password)) return@run failure(UserError.WeakPassword)
            val user = userRepo.create(username, email, usersDomain.hashedWithSha256(password))
            invitationRepo.updateRegisterInvitation(invitation)
            channelRepo.addUserToChannel(user, invitation.channel, invitation.role)
            return@run success(user)
        }

    fun loginUser(
        username: String,
        password: String,
    ): Either<UserError, AuthenticatedUser> =
        trxManager.run {
            if (username.isBlank()) return@run failure(UserError.UsernameCannotBeBlank)
            if (password.isBlank()) return@run failure(UserError.PasswordCannotBeBlank)
            userRepo.findByUsername(username, 1, 0).firstOrNull()
                ?: return@run failure(UserError.NoMatchingUsername)
            val userAuthenticated =
                userRepo.findByUsernameAndPassword(username, usersDomain.hashedWithSha256(password))
                    ?: return@run failure(UserError.NoMatchingPassword)
            val sessions = sessionRepo.findByUserId(userAuthenticated.id)
            if (sessions.size >= User.MAX_SESSIONS) {
                sessionRepo.deleteSession(sessions.first().token)
            }
            val session = sessionRepo.createSession(userAuthenticated.id, usersDomain.generateToken())
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
            val user = userRepo.findById(session.userId) ?: return@run failure(UserError.UserNotFound)
            if (newUsername.isBlank()) return@run failure(UserError.UsernameCannotBeBlank)
            if (newUsername.length > User.MAX_USERNAME_LENGTH) return@run failure(UserError.UsernameToLong)
            if (userRepo.findByUsername(newUsername, 1, 0).isNotEmpty()) {
                return@run failure(UserError.UsernameAlreadyExists)
            }
            val userEdited = userRepo.updateUsername(user, newUsername)
            return@run success(userEdited)
        }

    fun deleteUser(token: String): Either<UserError, User> =
        trxManager.run {
            val session = sessionRepo.findByToken(token) ?: return@run failure(UserError.Unauthorized)
            val user = userRepo.findById(session.userId) ?: return@run failure(UserError.UserNotFound)
            if (session.expired()) {
                sessionRepo.deleteSession(token)
                return@run failure(UserError.SessionExpired)
            }
            sessionRepo.findByUserId(session.userId).forEach { sessionRepo.deleteSession(it.token) }
            val invitations = invitationRepo.getInvitationsOfUser(user)
            invitations.forEach { invitationRepo.deleteChannelInvitationById(it.id) }
            channelRepo.getChannelsOfUser(user).forEach { channelRepo.leaveChannel(user, it) }
            val userDeleted = userRepo.delete(session.userId)
            return@run success(userDeleted)
        }

    fun clear() =
        trxManager.run {
            userRepo.clear()
        }
}
