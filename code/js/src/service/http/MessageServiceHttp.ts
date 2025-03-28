import {MessageService} from "../interfaces/MessageService";
import {Message} from "../../domain/Message";
import {handleResponse} from "./responseHandler";
import {messagesMapper} from "./mappers";

export class MessageServiceHttp implements MessageService {

    private baseUrl = 'http://localhost:8080/api/messages';

    async getMessages(channelId: number, limit: number = 10 , skip: number = 0): Promise<Message[]> {
        const response = await fetch(`${this.baseUrl}/history/${channelId}?limit=${limit}&skip=${skip}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json,  application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        });
        const json = await handleResponse(response);
        return messagesMapper(json);
    }

    async sendMessage(channelId: number, message: string): Promise<void> {
        const response = await fetch('http://localhost:8080/api/messages', {
            method: 'POST',
            headers: {
                'Accept': 'application/json,  application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(
                {
                    channelId: channelId,
                    content: message
                }),
        });
        await handleResponse(response);
    }
}