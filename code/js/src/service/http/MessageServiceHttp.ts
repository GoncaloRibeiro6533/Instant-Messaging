import { MessageService } from "../interfaces/MessageService";
import { Message } from "../../domain/Message";

export class MessageServiceHttp implements MessageService {
    async sendMessage(
        token: string,
        channelId: number,
        message: string
    ): Promise<Message> {
        try {
            const response = await fetch(`/api/messages`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ channelId, content: message }),
            });

            if (!response.ok) {
                throw new Error("Failed to send message");
            }

            return response.json();
        } catch (error) {
            console.error("Error sending message:", error);
            throw error;
        }
    }

    async getMessages(
        token: string,
        channelId: number,
        limit: number = 10,
        skip: number = 0
    ): Promise<Message[]> {
        try {
            const response = await fetch(
                `/api/messages/${channelId}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error("Failed to fetch message history");
            }

            return response.json();
        } catch (error) {
            console.error("Error fetching message history:", error);
            throw error;
        }
    }
}
