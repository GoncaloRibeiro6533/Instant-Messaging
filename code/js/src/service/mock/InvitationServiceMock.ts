import {  Repo } from "../../App";
import { InvitationService } from "../interfaces/InvitationService";
import { Channel } from "../../domain/Channel";
import { Role } from "../../domain/Role";
import { ChannelInvitation } from "../../domain/ChannelInvitation";
import { RegisterInvitation } from "../../domain/RegisterInvitation";
import {ChannelRepo} from "./repo/ChannelRepo";
import { tokenHandler } from "./tokenHandler";


export class InvitationServiceMock implements InvitationService {
    repo: Repo;
    channelRepo: ChannelRepo;

    constructor(repo: Repo) {
        this.repo = repo;
    }

    async getInvitationsOfUser(): Promise<Array<ChannelInvitation>> {
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.invitationRepo.getInvitationsOfUser(user);
    }
    async createRegisterInvitation(email: string, channelId: number, role: Role): Promise<RegisterInvitation> {
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.invitationRepo.createRegisterInvitation(user, email, this.repo.channelRepo.channels.find(channel => channel.id === channelId)!, role);
    }

    async createChannelInvitation(receiverId: number, channelId: number, role: Role): Promise<ChannelInvitation> {
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.invitationRepo.createChannelInvitation(user, receiverId, this.repo.channelRepo.channels.find(channel => channel.id === channelId)!, role);
    }

    async acceptChannelInvitation(invitationId: number): Promise<Channel> {
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
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

    async declineChannelInvitation(invitationId: number): Promise<Boolean> {
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.invitationRepo.declineChannelInvitation(user, invitationId);
    }
}