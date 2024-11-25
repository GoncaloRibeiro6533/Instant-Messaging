import { User } from './User';
import { Channel } from './Channel';

export type Message = {
    id: number,
    sender: User,
    channel: Channel,
    content: string,
    timestamp: Date,

}