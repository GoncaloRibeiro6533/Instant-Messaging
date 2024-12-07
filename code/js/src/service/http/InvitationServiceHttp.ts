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
        email: string,
        channelId: number,
        role: Role
    ): Promise<RegisterInvitation> {
        const response = await fetch(`${this.baseUrl}/register`, {
            method: "POST",
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({ email, channelId, role }),
        });
        const json = await handleResponse(response);
        return registerInvitationMapper(json);
    }

    async createChannelInvitation(
        receiverId: number,
        channelId: number,
        role: Role
    ): Promise<ChannelInvitation> {
        const response = await fetch(`${this.baseUrl}/channel`, {
            method: "POST",
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({ receiverId, channelId, role }),
        });
        const json = await handleResponse(response);
        return channelInvitationMapper(json)
    }

    async acceptChannelInvitation(
        invitationId: number
    ): Promise<Channel> {
        const response = await fetch(`${this.baseUrl}/accept/${invitationId}`, {
            method: "PUT",
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        });
        const json = await handleResponse(response);
        return new Channel(json.id, json.name, json.creator, json.visibility);
    }

    async declineChannelInvitation(
        invitationId: number
    ): Promise<Boolean> {
        const response = await fetch(`${this.baseUrl}/decline/${invitationId}`, {
            method: "PUT",
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        });
        return response.status == 200;
    }

    async getInvitationsOfUser(): Promise<Array<ChannelInvitation>> {
        const response = await fetch(`${this.baseUrl}/user/invitations`, {
            method: "GET",
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        });
        const json = await handleResponse(response);
        const invitations: ChannelInvitation[] = [];
        for (const invitation of json) {
            invitations.push(await channelInvitationMapper(invitation));
        }
        return invitations;
    }
}