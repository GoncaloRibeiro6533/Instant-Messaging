
import { Message } from "../../../domain/Message";
import { User } from "../../../domain/User";
import { Channel } from "../../../domain/Channel";
import { Visibility } from "../../../domain/Visibility";

export interface MessageRepoInterface {
    messages: Array<Message>;
    createMessage(user: User, channel: Channel, content: string, timestamp: Date): Message;
    getMessages(user: User, channel: Channel, limit: number, skip: number): Message[];

}

export class MessageRepo implements MessageRepoInterface {
    public messages: Message[] = [
        {
            id: 1,
            sender: {
                id: 1,
                username: 'Bob',
                email: 'bob@example.com'
            },
            channel: {
                id: 1,
                name: 'Channel 1',
                creator: {
                    id: 1,
                    username: 'Bob',
                    email: 'bob@example.com'
                },
                visibility: Visibility.PUBLIC
            },
            content: 'Hello',
            timestamp: new Date("2021-09-01T00:00:00Z")
        } 
    ]

    createMessage(user: User, channel: Channel, content: string, timestamp: Date): Message {
        const message: Message = {
            id: this.messages.length + 1,
            sender: user,
            channel,
            content,
            timestamp
        };
        this.messages.push(message);
        return message;
    }

    getMessages(user: User, channel: Channel, limit: number, skip: number): Message[] {
        return this.messages.filter(message => message.channel.id === channel.id).slice(skip, skip + limit);
    }


}