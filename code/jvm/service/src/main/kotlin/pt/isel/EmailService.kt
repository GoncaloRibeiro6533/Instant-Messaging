package pt.isel

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
// https://docs.spring.io/spring-framework/reference/integration/email.html

interface EmailServiceInterface {
    fun sendInvitationEmail(
        to: String,
        invitation: RegisterInvitation,
    )
}

@Service
class EmailService(private val mailSender: JavaMailSender) : EmailServiceInterface {
    private fun sendEmail(
        to: String,
        subject: String,
        text: String,
    ) {
        try {
            val message = SimpleMailMessage()
            message.setTo(to)
            message.subject = subject
            message.text = text
            mailSender.send(message)
        } catch (e: Exception) {
            println("Email sending failed: " + e.message)
        }
    }

    override fun sendInvitationEmail(
        to: String,
        invitation: RegisterInvitation,
    ) {
        val subject = "ChImp Invitation"
        val text =
            "You have been invited to join ChImp. " +
                "Click the following link to register: http://localhost:8000/register/${invitation.code}"
        try {
            sendEmail(to, subject, text)
        } catch (e: Exception) {
            // ignore for now
            return
        }
    }
}
