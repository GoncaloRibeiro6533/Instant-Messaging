import {Role} from "./Role";
import {User} from "./User";


export type RegisterInvitation = {
    id: number,
    sender: User,
    receiver: User,
    role: Role,
    isUsed: boolean,
    timestamp: Date,
}