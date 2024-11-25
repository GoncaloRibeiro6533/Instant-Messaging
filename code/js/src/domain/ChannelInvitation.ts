import { Channel } from "./Channel";
import { Role } from "./Role";
import { User } from "./User";



export type ChannelInvitation = {
    id: number,
    sender: User,
    receiver: User,
    channel: Channel,
    role: Role,
    isUsed: boolean,
    timestamp: Date,
}