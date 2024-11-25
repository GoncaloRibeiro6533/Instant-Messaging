import { UserService } from "./interfaces/UserService";
import { ChannelService } from "./interfaces/ChannelService";
import { MessageService } from "./interfaces/MessageService";

interface Service {
    
    userService : UserService
    channelService : ChannelService
    messageService : MessageService
    
}

export default Service;