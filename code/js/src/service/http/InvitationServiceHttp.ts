import { InvitationService } from "../interfaces/InvitationService";
import { Channel } from "../../domain/Channel";
import { Role } from "../../domain/Role";
import { ChannelInvitation } from "../../domain/ChannelInvitation";
import { RegisterInvitation } from "../../domain/RegisterInvitation";

export class InvitationServiceHttp implements InvitationService {
    async createRegisterInvitation(
        token: string,
        email: string,
        channelId: number,
        role: Role
    ): Promise<RegisterInvitation> {
        try {
            const response = await fetch(`/api/invitation/register`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ email, channelId, role }),
            });

            if (!response.ok) throw new Error("Failed to create register invitation");
            return response.json();
        } catch (error) {
            console.error("Error creating register invitation:", error);
            throw error;
        }
    }

    async createChannelInvitation(
        token: string,
        receiverId: number,
        channelId: number,
        role: Role
    ): Promise<ChannelInvitation> {
        try {
            const response = await fetch(`/api/invitation/channel`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ receiverId, channelId, role }),
            });

            if (!response.ok) throw new Error("Failed to create channel invitation");
            return response.json();
        } catch (error) {
            console.error("Error creating channel invitation:", error);
            throw error;
        }
    }

    async acceptChannelInvitation(
        token: string,
        invitationId: number,
        userId: number
    ): Promise<Channel> {
        try {
            const response = await fetch(`/api/invitation/accept/${invitationId}`, {
                method: "PUT",
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) throw new Error("Failed to accept channel invitation");
            return response.json();
        } catch (error) {
            console.error("Error accepting channel invitation:", error);
            throw error;
        }
    }

    async declineChannelInvitation(
        token: string,
        invitationId: number,
        userId: number
    ): Promise<boolean> {
        try {
            const response = await fetch(`/api/invitation/decline/${invitationId}`, {
                method: "PUT",
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) throw new Error("Failed to decline channel invitation");
            return response.ok;
        } catch (error) {
            console.error("Error declining channel invitation:", error);
            throw error;
        }
    }

    async getInvitationsOfUser(token: string): Promise<Array<ChannelInvitation>> {
        try {
            const response = await fetch(`/api/invitation/user/invitations`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) throw new Error("Failed to fetch user invitations");
            return response.json();
        } catch (error) {
            console.error("Error fetching user invitations:", error);
            throw error;
        }
    }

    getRegisterInvitationById(token: string, invitationId: number): Promise<RegisterInvitation> {
        return Promise.resolve(undefined);
        //TODo this function does not exist in the backend
    }

}