package pt.isel.talkRooms

import org.springframework.test.context.ActiveProfiles

class MessageControllerTests {
    @ActiveProfiles("inMem")
    class MessageControllerTests : AbstractMessageControllerTest()
}
