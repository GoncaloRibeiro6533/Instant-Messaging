import { Message } from "../../domain/Message";

export interface MessageService {
    getMessages(channelId: number, limit?: number, skip?: number): Promise<Message[]>;
    sendMessage(channelId: number, message: string): Promise<Message>|Promise<void>;
}