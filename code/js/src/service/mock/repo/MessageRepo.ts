
import { Message } from "../../../domain/Message";
import { User } from "../../../domain/User";
import { Channel } from "../../../domain/Channel";
import { Visibility } from "../../../domain/Visibility";

export interface MessageRepoInterface {
    messages: Array<Message>;
    createMessage(user: User, channel: Channel, content: string, timestamp: Date): Message;
    getMessages(channel: Channel, limit: number, skip: number): Message[];

}

export class MessageRepo implements MessageRepoInterface {
    public messages: Message[] = [
        new Message(
            1,
            new User(1, 'Bob', 'bob@example.com'),
            new Channel( 1, 'Channel 1', new User(1, 'Bob', 'bob@example.com'), Visibility.PUBLIC),
            'Hello',
            new Date("2024-11-26T12:33:24Z")
        )
        ,
    ]

    createMessage(user: User, channel: Channel, content: string, timestamp: Date): Message {
        const message: Message = new Message(
            this.messages.length + 1,
            user,
            channel,
            content,
            timestamp
        );
        this.messages.push(message);
        return message;
    }

    getMessages(channel: Channel, limit: number, skip: number): Message[] {
        return this.messages.filter(message => message.channel.id === channel.id).slice(skip, skip + limit);
    }


}