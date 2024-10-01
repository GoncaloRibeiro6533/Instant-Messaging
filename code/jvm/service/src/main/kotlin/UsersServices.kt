class UsersServices(private val userRepository : UsersRepository) {

    fun getUserById(id: Int) : User {
        return userRepository.findUserById(id)
    }
    fun createUser(username: String, password: String) : User {
        return userRepository.createUser(username, password)
    }

    fun findUserByUsername(username: String) : List<User> {
        return userRepository.findUserByUsername(username)
    }
}