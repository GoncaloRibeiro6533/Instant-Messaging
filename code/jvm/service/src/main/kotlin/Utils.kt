
sealed class AuthenticationError {
    data object Unauthorized : AuthenticationError()
    data object SessionExpired: AuthenticationError()
    data object UserNotFound : AuthenticationError()
}

fun authenticate(trxManager: TransactionManager, token: String): Either<AuthenticationError, User> =
    trxManager.run {
        val session = sessionRepo.findByToken(token) ?: return@run failure(AuthenticationError.Unauthorized)
        if (session.expiration < Instant.now()) {
            sessionRepo.delete(session)
            return@run failure(AuthenticationError.SessionExpired)
        }
        val user = userRepo.findById(session.userId) ?: return@run failure(AuthenticationError.UserNotFound)
        return@run success(user)
    }