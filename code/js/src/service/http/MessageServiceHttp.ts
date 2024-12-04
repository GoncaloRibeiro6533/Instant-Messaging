import {MessageService} from "../interfaces/MessageService";
import {Message} from "../../domain/Message";

export class MessageServiceHttp implements MessageService {
    getMessages(token: string, channelId: number, limit: number, skip: number): Promise<Message[]> {
        return Promise.resolve([]);
    }

    sendMessage(token: string, channelId: number, message: string): Promise<Message> {
        return Promise.resolve(undefined);
    }

}