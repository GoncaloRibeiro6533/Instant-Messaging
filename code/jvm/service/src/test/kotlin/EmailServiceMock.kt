import pt.isel.EmailServiceInterface
import pt.isel.RegisterInvitation

class EmailServiceMock : EmailServiceInterface {
    override fun sendInvitationEmail(
        to: String,
        invitation: RegisterInvitation,
    ) {
        println("Email sent to $to with invitation code ${invitation.code}")
    }
}
