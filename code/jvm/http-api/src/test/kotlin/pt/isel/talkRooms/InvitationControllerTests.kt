package pt.isel.talkRooms

import org.springframework.test.context.ActiveProfiles

class InvitationControllerTests {
    @ActiveProfiles("inMem")
    class InvitationControllerTest : AbstractInvitationControllerTest()
}
