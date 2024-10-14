package models

data class UserInputModel(val username: String, val email: String, val password: String)

data class UserOutputModel(val id: Int, val username: String, val email: String)
