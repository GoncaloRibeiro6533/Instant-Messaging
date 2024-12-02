import {Role} from "../../domain/Role";
import {Channel} from "../../domain/Channel";
import {ChannelInvitation} from "../../domain/ChannelInvitation";
import {RegisterInvitation} from "../../domain/RegisterInvitation";

export interface InvitationService {
    getInvitationsOfUser(token: string): Promise<Array<ChannelInvitation>>;
    getRegisterInvitationById(token: string, invitationId: number): Promise<RegisterInvitation>;
    createRegisterInvitation(token: string, email: string, channelId: number, role: Role,): Promise<RegisterInvitation>;
    createChannelInvitation(token: string, receiverId: number, channelId: number, role: Role): Promise<ChannelInvitation>;
    acceptChannelInvitation(token: string, invitationId: number, userId: number): Promise<Channel>;
    declineChannelInvitation(token: string, invitationId: number, userId: number): Promise<Boolean>;
}