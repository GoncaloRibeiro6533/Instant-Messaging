import { repo, Repo } from "../../App";
import { InvitationService } from "../interfaces/InvitationService";
import { Channel } from "../../domain/Channel";
import { Role } from "../../domain/Role";
import { ChannelInvitation } from "../../domain/ChannelInvitation";
import { RegisterInvitation } from "../../domain/RegisterInvitation";

export class InvitationServiceMock implements InvitationService {
    repo: Repo;

    constructor(repo: Repo) {
        this.repo = repo;
    }

    async getInvitationsOfUser(token: string): Promise<Array<ChannelInvitation>> {
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.invitationRepo.getInvitationsOfUser(user);
    }

    async getRegisterInvitationById(token: string, invitationId: number): Promise<RegisterInvitation> {
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.invitationRepo.getRegisterInvitationById(user, invitationId);
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

    async acceptChannelInvitation(token: string, invitationId: number, userId: number): Promise<Channel> {
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.invitationRepo.acceptChannelInvitation(user, invitationId, userId);
    }

    async declineChannelInvitation(token: string, invitationId: number, userId: number): Promise<Boolean> {
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.invitationRepo.declineChannelInvitation(user, invitationId, userId);
    }
}