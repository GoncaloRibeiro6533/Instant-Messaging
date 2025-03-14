package pt.isel.models

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

private const val MEDIA_TYPE = "application/problem+json"
private const val PROBLEM_URI_PATH =
    "https://github.com/isel-leic-daw/2024-daw-leic53d-g06-53d/tree/main/docs/talkRooms/problems"

sealed class Problem(
    typeUri: URI,
) {
    @Suppress("unused")
    val type = typeUri.toString()
    val title = typeUri.toString().split("/").last()

    fun response(status: HttpStatus): ResponseEntity<Any> =
        ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body(this)

    data object InvalidText : Problem(URI("$PROBLEM_URI_PATH/invalid-text"))

    data object MessageNotFound : Problem(URI("$PROBLEM_URI_PATH/message-not-found"))

    data object UserNotInChannel : Problem(URI("$PROBLEM_URI_PATH/user-not-in-channel"))

    data object InvalidRole : Problem(URI("$PROBLEM_URI_PATH/invalid-role"))

    data object InvalidChannel : Problem(URI("$PROBLEM_URI_PATH/invalid-channel"))

    data object InvalidReceiver : Problem(URI("$PROBLEM_URI_PATH/invalid-receiver"))

    data object SenderDoesntBelongToChannel : Problem(URI("$PROBLEM_URI_PATH/sender-doesnt-belong-to-channel"))

    data object InvitationExpired : Problem(URI("$PROBLEM_URI_PATH/invitation-expired"))

    data object AlreadyInChannel : Problem(URI("$PROBLEM_URI_PATH/already-in-channel"))

    data object CantInviteToPublicChannel : Problem(URI("$PROBLEM_URI_PATH/cant-invite-to-public-channel"))

    data object ChannelNameAlreadyExists : Problem(URI("$PROBLEM_URI_PATH/channel-name-already-exists"))

    data object InvalidChannelName : Problem(URI("$PROBLEM_URI_PATH/invalid-channel-name"))

    data object UserAlreadyInChannel : Problem(URI("$PROBLEM_URI_PATH/user-already-in-channel"))

    data object InvalidVisibility : Problem(URI("$PROBLEM_URI_PATH/invalid-visibility"))

    data object InvalidIdentifier : Problem(URI("$PROBLEM_URI_PATH/invalid-identifier"))

    data object NoMatchingUsername : Problem(URI("$PROBLEM_URI_PATH/no-matching-username"))

    data object NoMatchingPassword : Problem(URI("$PROBLEM_URI_PATH/no-matching-password"))

    data object WeakPassword : Problem(URI("$PROBLEM_URI_PATH/weak-password"))

    data object NotFirstUser : Problem(URI("$PROBLEM_URI_PATH/not-first-user"))

    data object NegativeIdentifier : Problem(URI("$PROBLEM_URI_PATH/negative-identifier"))

    data object EmailAlreadyInUse : Problem(URI("$PROBLEM_URI_PATH/email-already-in-use"))

    data object UserNotFound : Problem(URI("$PROBLEM_URI_PATH/user-not-found"))

    data object UsernameAlreadyInUse : Problem(URI("$PROBLEM_URI_PATH/username-already-in-use"))

    data object UsernameToLong : Problem(URI("$PROBLEM_URI_PATH/username-too-long"))

    data object NegativeSkip : Problem(URI("$PROBLEM_URI_PATH/negative-skip"))

    data object NegativeLimit : Problem(URI("$PROBLEM_URI_PATH/negative-limit"))

    data object UsernameCannotBeBlank : Problem(URI("$PROBLEM_URI_PATH/username-cannot-be-blank"))

    data object PasswordCannotBeBlank : Problem(URI("$PROBLEM_URI_PATH/password-cannot-be-blank"))

    data object EmailCannotBeBlank : Problem(URI("$PROBLEM_URI_PATH/email-cannot-be-blank"))

    data object InvalidEmail : Problem(URI("$PROBLEM_URI_PATH/invalid-email"))

    data object InvalidPassword : Problem(URI("$PROBLEM_URI_PATH/invalid-password"))

    data object SessionExpired : Problem(URI("$PROBLEM_URI_PATH/session-expired"))

    data object Unauthorized : Problem(URI("$PROBLEM_URI_PATH/unauthorized"))

    data object EmailDoesNotMatchInvite : Problem(URI("$PROBLEM_URI_PATH/email-does-not-match-invite"))

    data object InvitationAlreadyUsed : Problem(URI("$PROBLEM_URI_PATH/invitation-already-used"))

    data object ChannelNotFound : Problem(URI("$PROBLEM_URI_PATH/channel-not-found"))

    data object InvitationNotFound : Problem(URI("$PROBLEM_URI_PATH/invitation-not-found"))

    data object MessageTooLong : Problem(URI("$PROBLEM_URI_PATH/message-too-long"))

    data object InvalidRequestContent : Problem(URI("$PROBLEM_URI_PATH/invalid-request-content"))
}
