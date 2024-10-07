interface UserRepository {

    fun findById(id: Int): User?
    fun findUserByToken(token: String): User?
    fun findUserByUsername(username: String, limit: Int, skip: Int) : List<User>
    fun createUser(username: String, password: String, token: String) : User
    fun updateUsername(token: String, newUsername: String) : User
    fun validateLogin(username: String, password: String): User?
    fun delete(id: Int): User
    fun clear(): Unit
}