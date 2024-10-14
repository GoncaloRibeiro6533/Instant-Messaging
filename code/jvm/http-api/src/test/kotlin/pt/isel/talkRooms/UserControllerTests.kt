package pt.isel.talkRooms

import org.springframework.test.context.ActiveProfiles

class UserControllerTests {

    @ActiveProfiles("inMem")
    class UsersControllerTests : AbstractUserControllerTest()
}