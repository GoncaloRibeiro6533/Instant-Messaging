package pt.isel.models.user

data class UserList(
    val users: List<UserIdentifiers>,
    val nUsers: Int,
)
