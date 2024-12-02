import {Role} from "./Role";
import {User} from "./User";
import {Channel} from "./Channel";


export type RegisterInvitation = {
    id: number,
    sender: User,
    email: string,
    channel: Channel,
    role: Role,
    isUsed: boolean,
    timestamp: Date,
}