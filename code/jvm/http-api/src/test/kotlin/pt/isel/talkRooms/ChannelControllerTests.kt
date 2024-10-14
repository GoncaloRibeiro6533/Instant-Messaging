package pt.isel.talkRooms

import org.springframework.test.context.ActiveProfiles

class ChannelControllerTests {

    @ActiveProfiles("inMem")
    class ChannelControllerTests : AbstractChannelControllerTest()
}