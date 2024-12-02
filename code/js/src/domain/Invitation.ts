import { User } from './User';


export type Invitation = {
    id: number,
    sender: User,
    isUsed: boolean,
    timestamp: Date
}