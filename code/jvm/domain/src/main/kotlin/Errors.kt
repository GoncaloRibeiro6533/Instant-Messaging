/**
 * Class that represents the errors that can occur in the application
 * @property message the error message
 */

sealed class Errors(override val message: String) : Exception(message) {
    class NotAuthorizedException(message: String) : Errors(message)

    class NotFoundException(message: String) : Errors(message)

    class BadRequestException(message: String) : Errors(message)

    class InternalServerErrorException(message: String) : Errors(message)
}