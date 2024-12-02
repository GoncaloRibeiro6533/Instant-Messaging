import { User } from './User';
import { Channel } from './Channel';

export class Message {
    id: number;
    sender: User;
    channel: Channel;
    content: string;
    timestamp: Date;

    constructor(id: number, sender: User, channel: Channel, content: string, timestamp: Date) {
        this.id = id;
        this.sender = sender;
        this.channel = channel;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Método estático para criar uma instância de Message a partir de um objeto 
    public static fromRaw(raw: any): Message {
        return new Message(
            raw.id,
            User.fromRaw(raw.sender), // Converte o raw para uma instância de User
            Channel.fromRaw(raw.channel), // Converte o raw para uma instância de Channel
            raw.content,
            new Date(raw.timestamp) // Converte o timestamp para uma instância de Date
        );
    }

    // Método para serializar uma instância de Message para um objeto simples
    public toRaw(): any {
        return {
            id: this.id,
            sender: this.sender.toRaw(), // Serializa o User
            channel: this.channel.toRaw(), // Serializa o Channel
            content: this.content,
            timestamp: this.timestamp.toISOString(), // Serializa o Date como string ISO
        };
    }

    public equals(other: Message): boolean {
        return this.id === other.id &&
            this.sender.equals(other.sender) &&
            this.channel.equals(other.channel) &&
            this.content === other.content &&
            this.timestamp.getTime() === other.timestamp.getTime();
    }
}
