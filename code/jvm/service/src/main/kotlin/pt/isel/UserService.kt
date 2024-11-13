package pt.isel

import jakarta.inject.Named
import kotlinx.datetime.Clock

sealed class UserError {
    data object UserNotFound : UserError()

    data object UsernameAlreadyExists : UserError()

    data object NoMatchingUsername : UserError()

    data object NoMatchingPassword : UserError()

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
class UserService(
    private val trxManager: TransactionManager,
    private val usersDomain: UsersDomain,
    private val clock: Clock,
) {
    fun addFirstUser(
        username: String,
        email: String,
        password: String,
    ) = trxManager.run {
        if (userRepo.findAll().isNotEmpty()) return@run failure(UserError.NotFirstUser)
        if (username.isBlank()) return@run failure(UserError.UsernameCannotBeBlank)
        if (password.isBlank()) return@run failure(UserError.PasswordCannotBeBlank)
        if (username.length > User.MAX_USERNAME_LENGTH) return@run failure(UserError.UsernameToLong)
        if (email.isBlank()) return@run failure(UserError.EmailCannotBeBlank)
        if (!usersDomain.isValidEmail(email)) return@run failure(UserError.InvalidEmail)
        if (!usersDomain.isPasswordStrong(password)) return@run failure(UserError.WeakPassword)
        val passwordValidationInfo = usersDomain.createPasswordValidationInformation(password)
        val user = userRepo.createUser(username, email, passwordValidationInfo.validationInfo)
        return@run success(user)
    }

    fun createUser(
        username: String,
        email: String,
        password: String,
        inviteId: Int,
    ): Either<UserError, User> =
        trxManager.run {
            if (inviteId < 0) return@run failure(UserError.NegativeIdentifier)
            var invitation =
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
            val passwordValidationInfo = usersDomain.createPasswordValidationInformation(password)
            val user = userRepo.createUser(username, email, passwordValidationInfo.validationInfo)
            invitation = invitation.markAsUsed()
            invitationRepo.updateRegisterInvitation(invitation)
            channelRepo.addUserToChannel(user, invitation.channel, invitation.role)
            return@run success(user)
        }

    fun loginUser(
        username: String,
        password: String,
    ): Either<UserError, AuthenticatedUser> =
        trxManager.run {
            if (password.isBlank()) return@run failure(UserError.PasswordCannotBeBlank)
            if (username.isBlank()) return@run failure(UserError.UsernameCannotBeBlank)
            val user =
                userRepo.findByUsername(username, 1, 0).firstOrNull {
                    it.username == username
                } ?: return@run failure(UserError.NoMatchingUsername)
            val repoPassword = userRepo.findPasswordOfUser(user)
            val passwordValidationInfo = PasswordValidationInfo(repoPassword)
            if (!usersDomain.validatePassword(password, passwordValidationInfo)) {
                    return@run failure(UserError.NoMatchingPassword)
                }
            val now = clock.now()
            val newToken =
                Token(
                    token = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                    userId = user.id,
                    createdAt = now,
                    lastUsedAt = now,
                )
            sessionRepo.createSession(user.id, newToken, usersDomain.maxNumberOfTokensPerUser)
            return@run success(AuthenticatedUser(user, newToken.token.validationInfo))
        }

    fun logoutUser(token: String) =
        trxManager.run {
            sessionRepo.findByToken(token)?.let { userRepo.findById(it.userId) }
                ?: return@run failure(UserError.SessionExpired)
            sessionRepo.deleteSession(token)
            return@run success(true)
        }

    fun getUserById(id: Int): Either<UserError, User> =
        trxManager.run {
            if (id < 0) return@run failure(UserError.NegativeIdentifier)
            val user = userRepo.findById(id) ?: return@run failure(UserError.UserNotFound)
            return@run success(user)
        }

    fun findUserByUsername(
        username: String,
        limit: Int = 10,
        skip: Int = 0,
    ): Either<UserError, List<User>> =
        trxManager.run {
            if (username.isBlank()) return@run failure(UserError.UsernameCannotBeBlank)
            if (limit < 0) return@run failure(UserError.NegativeLimit)
            if (skip < 0) return@run failure(UserError.NegativeSkip)
            val users = userRepo.findByUsername(username, limit, skip)
            return@run success(users)
        }

    fun updateUsername(
        userId: Int,
        newUsername: String,
    ): Either<UserError, User> =
        trxManager.run {
            val user = userRepo.findById(userId) ?: return@run failure(UserError.UserNotFound)
            if (newUsername.isBlank()) return@run failure(UserError.UsernameCannotBeBlank)
            if (newUsername.length > User.MAX_USERNAME_LENGTH) return@run failure(UserError.UsernameToLong)
            if (userRepo.findByUsername(newUsername, 1, 0).isNotEmpty()) {
                return@run failure(UserError.UsernameAlreadyExists)
            }
            val userEdited = userRepo.updateUsername(user, newUsername)
            return@run success(userEdited)
        }

    fun deleteUser(userId: Int): Either<UserError, Unit> =
        trxManager.run {
            userRepo.findById(userId) ?: return@run failure(UserError.UserNotFound)
            val userDeleted = userRepo.delete(userId)
            return@run success(userDeleted)
        }

    fun getUserByToken(token: String): User? =
        trxManager.run {
            val session = sessionRepo.findByToken(token) ?: return@run null
            return@run userRepo.findById(session.userId) //TODO token does not expire, do that
        }
}
