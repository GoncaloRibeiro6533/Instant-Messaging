import { Message } from "../../domain/Message";

export interface MessageService {
    getMessages(token: string,channelId: number, limit?: number, skip?: number): Promise<Message[]>;
    sendMessage(token: string, channelId: number, message: string): Promise<Message>|Promise<void>;
}