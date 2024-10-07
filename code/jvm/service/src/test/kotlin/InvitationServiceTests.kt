import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class InvitationServiceTests {


    private lateinit var invitationService: InvitationService

    @BeforeEach
    fun setUp() {
        val trxManager = TransactionManagerInMem()
        invitationService = InvitationService(trxManager)
    }

    @Test
    fun `getInvitationsOfUser should return Unauthorized if token is invalid`() {
        val result = invitationService.getInvitationsOfUser(1, "invalidToken")
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.Unauthorized, result.value)
    }

    @Test
    fun `getInvitationsOfUser should return NegativeIdentifier error if userId is negative`() {
        val result = invitationService.getInvitationsOfUser(-1, "validToken")
        assertIs<Failure<InvitationError>>(result)
        assertEquals(InvitationError.NegativeIdentifier, result.value)
    }

}