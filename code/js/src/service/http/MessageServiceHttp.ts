import {MessageService} from "../interfaces/MessageService";
import {Message} from "../../domain/Message";
import {handleResponse} from "./responseHandler";
import {messagesMapper} from "./mappers";

export class MessageServiceHttp implements MessageService {
    
    async getMessages(token: string, channelId: number, limit?: number, skip?: number): Promise<Message[]> {
        const params = new URLSearchParams();
        limit && params.append('limit', limit.toString());
        skip && params.append('skip', skip.toString());
        const response = await fetch('http://localhost:8080/api/messages/history/' + channelId + '?' + params, {
            method: 'get',
            headers: {
                'Accept': 'application/json,  application/problem+json',
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
        });
        const json = await handleResponse(response);
        return messagesMapper(json);
    }

    async sendMessage(token: string, channelId: number, message: string): Promise<void> {
        const response = await fetch('http://localhost:8080/api/messages', {
            method: 'POST',
            headers: {
                'Accept': 'application/json,  application/problem+json',
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify(
                {
                    channelId: channelId,
                    content: message
                }),
        });
        const json = await handleResponse(response);
    }
}