import Errors.*
import java.security.MessageDigest

class UserServices(private val userRepository : UserRepository) {

    fun getUserById(id: Int, token: String) : User {
        isValidToken(token)
        if (id < 0) throw BadRequestException("Id must be greater than 0")
        return userRepository.findUserById(id) ?: throw NotFoundException("User not found")
    }

    fun loginUser(username: String, password: String): User {
        if (username.isBlank()) throw BadRequestException("Username must not be blank")
        if (password.isBlank()) throw BadRequestException("Password must not be blank")
        val user = userRepository.findUserByUsername(username, 10, 0)
        return userRepository.validateLogin(username, password.hashedWithSha256())
            ?: throw UnauthorizedException("Invalid username or password")
    }

    fun createUser(username: String, password: String): User {
        if (username.isBlank()) throw BadRequestException("Username must not be blank")
        if (password.isBlank()) throw BadRequestException("Password must not be blank")
        if (username.length > User.MAX_USERNAME_LENGTH)
            throw BadRequestException("Username must be less than ${User.MAX_USERNAME_LENGTH} characters")
        if (userRepository.findUserByUsername(username, 10, 0).any { it.username == username })
            throw BadRequestException("Username already exists")
        return userRepository.createUser(username, password.hashedWithSha256())
    }

    fun findUserByUsername(username: String, token: String, limit: Int = 10, skip: Int = 0) : List<User> {
        if (username.isBlank()) throw BadRequestException("Username must not be blank")
        isValidToken(token)
        val users =  userRepository.findUserByUsername(username, 10, 0)
        if (users.isEmpty()) throw NotFoundException("User not found")
        return users
    }

    fun updateUsername(token: String, newUsername: String) : User {
        val user = isValidToken(token)
        if (newUsername.isBlank()) throw BadRequestException("Username must not be blank")
        if (newUsername.length > User.MAX_USERNAME_LENGTH)
            throw BadRequestException("Username must be less than ${User.MAX_USERNAME_LENGTH} characters")
        if (userRepository.findUserByUsername(newUsername, 10, 0).any { it.username == newUsername })
            throw BadRequestException("Username already exists")
        return userRepository.updateUsername(token, newUsername)
    }

    fun getUnreadMessages(userId: Int): Map<Channel, List<Message>> {
        val user = userRepository.findUserById(userId)
            ?: throw NotFoundException("User not found")
        return user.unreadMessages.groupBy { it.channel }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun String.hashedWithSha256() =
        MessageDigest.getInstance("SHA-256")
            .digest(toByteArray())
            .toHexString()

    fun isValidToken(token: String) =
        userRepository.findUserByToken(token)
            ?: throw UnauthorizedException("Not autenticated")
}