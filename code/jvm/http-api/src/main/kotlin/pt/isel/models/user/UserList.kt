package pt.isel.models.user

data class UserList(
    val nUsers: Int,
    val users: List<UserIdentifiers>,
)
