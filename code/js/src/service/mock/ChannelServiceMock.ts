import {ChannelService} from '../interfaces/ChannelService';
import {Channel} from '../../domain/Channel';
import {Role} from '../../domain/Role';
import {ChannelMember} from '../../domain/ChannelMember';
import {Repo} from '../../App';
import {delay} from './utils';

export class ChannelServiceMock implements ChannelService {
    repo: Repo;

    constructor(repo: Repo) {
      this.repo = repo;
    }
    createChannel(name: string, visibility: string): Promise<Channel> {
        throw new Error('Method not implemented.');
    }
    joinChannel(channelId: number, role: Role): Promise<Channel> {
        throw new Error('Method not implemented.');
    }
    searchChannelByName(name: string, limit: number, skip: number): Promise<Channel[]> {
        throw new Error('Method not implemented.');
    }
    getChannelMembers(channelId: number): Promise<ChannelMember[]> {
        throw new Error('Method not implemented.');
    }
    getChannelsOfUser(userId: number): Promise<Map<Channel, Role>> {
        throw new Error('Method not implemented.');
    }
    updateChannelName(channelId: number, newName: string): Promise<Channel> {
        throw new Error('Method not implemented.');
    }
    getChannelById(channelId: number): Promise<Channel> {
        throw new Error('Method not implemented.');
    }
    leaveChannel(channelId: number): Promise<Channel> {
        throw new Error('Method not implemented.');
    }
}

/*
    async createChannel(token: string, name: string, visibility: string): Promise<Channel> {
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.channelRepo.createChannel(user, name, visibility);
    }

    async joinChannel(token: string, channelId: number, role: Role): Promise<Channel> {
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.channelRepo.joinChannel(user, channelId, role);
    }

    async searchChannelByName(token: string, name: string, limit: number, skip: number): Promise<Channel[]> {
        const user = this.repo.userRepo.getUserByToken(token);
        if (!user) {
            throw new Error("Invalid token");
        }
        const userChannels = this.repo.channelRepo.getChannelsOfUser(user, user.id);
        const channels= this.repo.channelRepo.searchChannelByName(name, limit, skip);
        return channels.filter(channel => channel.visibility === 'PUBLIC' || userChannels.has(channel));
    }

    async getChannelMembers(token: string, channelId: number): Promise<ChannelMember[]> {
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.channelRepo.getChannelMembers(user, channelId);
    }

    async getChannelsOfUser(token: string, userId: number): Promise<Map<Channel,Role>> {
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.channelRepo.getChannelsOfUser(user, userId);
    }

    async updateChannelName(token: string, channelId: number, newName: string): Promise<Channel> {
        const user = this.repo.userRepo.getUserByToken(token);
        if (!user) {
            throw new Error("Invalid token");
        }
        const channel = this.repo.channelRepo.channels.find(channel => channel.id === channelId);
        if (!channel) {
            throw new Error("Channel not found");
        }
        channel.name = newName;
        return channel;
    }

    async leaveChannel(token: string, channelId: number): Promise<Channel> {
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        const channel = this.repo.channelRepo.channels.find(channel => channel.id === channelId);
        if (!channel) {
            throw new Error('Channel not found');
        }
        const channelMember = this.repo.channelRepo.channelMembers.get(channel)!.find(member => member.user.id === user.id);
        if (!channelMember) {
            throw new Error('User not in channel');
        }
        this.repo.channelRepo.channelMembers.set(channel, this.repo.channelRepo.channelMembers.get(channel)!.filter(member => member.user.id !== user.id));
        return channel;
    }

    async deleteChannel(token: string, channelId: number): Promise<void> {
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        const channel = this.repo.channelRepo.channels.find(channel => channel.id === channelId);
        if (!channel) {
            throw new Error('Channel not found');
        }
        this.repo.channelRepo.channels = this.repo.channelRepo.channels.filter(channel => channel.id !== channelId);
        this.repo.channelRepo.channelMembers.delete(channel);
    }

    async getChannelById(token: string, channelId: number): Promise<Channel> {
        const user = this.repo.userRepo.getUserByToken(token)
        await delay(500)
        if (!user) {
            throw new Error("Invalid token");
        }
        const channel_members = this.repo.channelRepo.getChannelMembers(user, channelId);
        if(!channel_members.find(member => member.user.id === user.id)) {
            throw new Error("User not in channel");
        }
        const channel = this.repo.channelRepo.channels.find(channel => channel.id === channelId);
        if (!channel) {
            throw new Error('Channel not found');
        }
        return channel
    }
}*/