import {InvitationService} from "../interfaces/InvitationService";
import {Channel} from "../../domain/Channel";
import {Role} from "../../domain/Role";
import {ChannelInvitation} from "../../domain/ChannelInvitation";
import {RegisterInvitation} from "../../domain/RegisterInvitation";

export class InvitationServiceHttp implements InvitationService {
    acceptChannelInvitation(token: string, invitationId: number, userId: number): Promise<Channel> {
        return Promise.resolve(undefined);
    }

    createChannelInvitation(token: string, receiverId: number, channelId: number, role: Role): Promise<ChannelInvitation> {
        return Promise.resolve(undefined);
    }

    createRegisterInvitation(token: string, email: string, channelId: number, role: Role): Promise<RegisterInvitation> {
        return Promise.resolve(undefined);
    }

    declineChannelInvitation(token: string, invitationId: number, userId: number): Promise<Boolean> {
        return Promise.resolve(undefined);
    }

    getInvitationsOfUser(token: string): Promise<Array<ChannelInvitation>> {
        return Promise.resolve(undefined);
    }

    getRegisterInvitationById(token: string, invitationId: number): Promise<RegisterInvitation> {
        return Promise.resolve(undefined);
    }

}