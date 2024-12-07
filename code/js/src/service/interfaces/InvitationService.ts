import {Role} from "../../domain/Role";
import {Channel} from "../../domain/Channel";
import {ChannelInvitation} from "../../domain/ChannelInvitation";
import {RegisterInvitation} from "../../domain/RegisterInvitation";

export interface InvitationService {
    getInvitationsOfUser(): Promise<Array<ChannelInvitation>>;
    createRegisterInvitation(email: string, channelId: number, role: Role,): Promise<RegisterInvitation>;
    createChannelInvitation(receiverId: number, channelId: number, role: Role): Promise<ChannelInvitation>;

    acceptChannelInvitation(invitationId: number): Promise<Channel>;

    declineChannelInvitation(invitationId: number): Promise<Boolean>;
}