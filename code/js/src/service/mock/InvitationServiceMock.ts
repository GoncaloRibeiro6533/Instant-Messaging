import {  Repo } from "../../App";
import { InvitationService } from "../interfaces/InvitationService";
import { Channel } from "../../domain/Channel";
import { Role } from "../../domain/Role";
import { ChannelInvitation } from "../../domain/ChannelInvitation";
import { RegisterInvitation } from "../../domain/RegisterInvitation";
import {ChannelRepo} from "./repo/ChannelRepo";

export class InvitationServiceMock implements InvitationService {
    repo: Repo;
    channelRepo: ChannelRepo;

    constructor(repo: Repo) {
        this.repo = repo;
    }
    getInvitationsOfUser(): Promise<Array<ChannelInvitation>> {
        throw new Error("Method not implemented.");
    }
    createRegisterInvitation(email: string, channelId: number, role: Role): Promise<RegisterInvitation> {
        throw new Error("Method not implemented.");
    }
    createChannelInvitation(receiverId: number, channelId: number, role: Role): Promise<ChannelInvitation> {
        throw new Error("Method not implemented.");
    }
    acceptChannelInvitation(invitationId: number): Promise<Channel> {
        throw new Error("Method not implemented.");
    }
    declineChannelInvitation(invitationId: number): Promise<Boolean> {
        throw new Error("Method not implemented.");
    }
}
/*
    async getInvitationsOfUser(token: string): Promise<Array<ChannelInvitation>> {
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.invitationRepo.getInvitationsOfUser(user);
    }
    async createRegisterInvitation(token: string, email: string, channelId: number, role: Role): Promise<RegisterInvitation> {
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.invitationRepo.createRegisterInvitation(user, email, this.repo.channelRepo.channels.find(channel => channel.id === channelId)!, role);
    }

    async createChannelInvitation(token: string, receiverId: number, channelId: number, role: Role): Promise<ChannelInvitation> {
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.invitationRepo.createChannelInvitation(user, receiverId, this.repo.channelRepo.channels.find(channel => channel.id === channelId)!, role);
    }

    async acceptChannelInvitation(token: string, invitationId: number): Promise<Channel> {
        const user = this.repo.userRepo.getUserByToken(token);
        if (!user) {
            throw new Error("Invalid token");
        }
        const invitation = this.repo.invitationRepo.invitations.find(invitation => invitation.id === invitationId);
        if (invitation) {
            const channelMember = {
                user,
                role: invitation.role
            };
            const channelMembers = this.repo.channelRepo.channelMembers.get(invitation.channel) || [];
            channelMembers.push(channelMember);
            this.repo.channelRepo.channelMembers.set(invitation.channel, channelMembers);
        }
        return invitation.channel;
    }

    async declineChannelInvitation(token: string, invitationId: number): Promise<Boolean> {
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.invitationRepo.declineChannelInvitation(user, invitationId);
}*/