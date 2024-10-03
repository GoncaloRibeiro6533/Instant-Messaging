interface UsersRepository {

    fun findUserById(id: Int): User

    fun findUserByUsername(username: String) : List<User>

    fun createUser(username: String, password: String) : User


}