import { UserService } from "./interfaces/UserService";
import { ChannelService } from "./interfaces/ChannelService";
import { MessageService } from "./interfaces/MessageService";
import { InvitationService } from "./interfaces/InvitationService";

interface Service {
    
    userService : UserService
    channelService : ChannelService
    messageService : MessageService
    invitationService : InvitationService
    
}

export default Service;