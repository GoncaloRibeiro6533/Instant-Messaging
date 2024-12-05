import { InvitationService } from "../interfaces/InvitationService";
import { Channel } from "../../domain/Channel";
import { Role } from "../../domain/Role";
import { ChannelInvitation } from "../../domain/ChannelInvitation";
import { RegisterInvitation } from "../../domain/RegisterInvitation";
import {handleResponse} from "./responseHandler";
import {channelInvitationMapper, registerInvitationMapper} from "./mappers";

export class InvitationServiceHttp implements InvitationService {

    private baseUrl = 'http://localhost:8080/api/invitation';

    async createRegisterInvitation(
        token: string,
        email: string,
        channelId: number,
        role: Role
    ): Promise<RegisterInvitation> {
        const response = await fetch(`${this.baseUrl}/register`, {
            method: "POST",
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, channelId, role }),
        });
        const json = await handleResponse(response);
        return registerInvitationMapper(json);
    }

    async createChannelInvitation(
        token: string,
        receiverId: number,
        channelId: number,
        role: Role
    ): Promise<ChannelInvitation> {
        const response = await fetch(`${this.baseUrl}/channel`, {
            method: "POST",
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ receiverId, channelId, role }),
        });
        const json = await handleResponse(response);
        return channelInvitationMapper(json)
    }

    async acceptChannelInvitation(
        token: string,
        invitationId: number
    ): Promise<Channel> {
        const response = await fetch(`${this.baseUrl}/accept/${invitationId}`, {
            method: "PUT",
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
        });
        const json = await handleResponse(response);
        return new Channel(json.id, json.name, json.creator, json.visibility);
    }

    async declineChannelInvitation(
        token: string,
        invitationId: number
    ): Promise<Boolean> {
        const response = await fetch(`${this.baseUrl}/decline/${invitationId}`, {
            method: "PUT",
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
        });
        return response.status == 200;
    }

    async getInvitationsOfUser(token: string): Promise<Array<ChannelInvitation>> {
        const response = await fetch(`${this.baseUrl}/user/invitations`, {
            method: "GET",
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            }
        });
        const json = await handleResponse(response);
        const invitations: ChannelInvitation[] = [];
        for (const invitation of json) {
            invitations.push(await channelInvitationMapper(invitation));
        }
        return invitations;
    }
}