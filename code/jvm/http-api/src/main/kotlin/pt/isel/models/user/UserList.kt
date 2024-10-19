package pt.isel.models.user

data class UserList(
    val users: List<UserIdentifiers>,
    val nrOfUsers: Int,
)
