import {ChannelService} from '../interfaces/ChannelService';
import {Channel} from '../../domain/Channel';
import {Role} from '../../domain/Role';
import {ChannelMember} from '../../domain/ChannelMember';
import {Repo} from '../../App';
import {delay} from './utils';
import { tokenHandler } from './tokenHandler';

export class ChannelServiceMock implements ChannelService {
    repo: Repo;

    constructor(repo: Repo) {
      this.repo = repo;
    }
    
    async createChannel(name: string, visibility: string): Promise<Channel> {
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.channelRepo.createChannel(user, name, visibility);
    }

    async joinChannel(channelId: number, role: Role): Promise<Channel> {
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.channelRepo.joinChannel(user, channelId, role);
    }

    async searchChannelByName(name: string, limit: number, skip: number): Promise<Channel[]> {
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
        const user = this.repo.userRepo.getUserByToken(token);
        if (!user) {
            throw new Error("Invalid token");
        }
        const userChannels = this.repo.channelRepo.getChannelsOfUser(user, user.id);
        const channels= this.repo.channelRepo.searchChannelByName(name, limit, skip);
        return channels.filter(channel => channel.visibility === 'PUBLIC' || userChannels.has(channel));
    }

    async getChannelMembers(channelId: number): Promise<ChannelMember[]> {
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.channelRepo.getChannelMembers(user, channelId);
    }

    async getChannelsOfUser(userId: number): Promise<Map<Channel,Role>> {
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
        const user = this.repo.userRepo.getUserByToken(token)
        if (!user) {
            throw new Error("Invalid token");
        }
        return this.repo.channelRepo.getChannelsOfUser(user, userId);
    }

    async updateChannelName(channelId: number, newName: string): Promise<Channel> {
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
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

    async leaveChannel(channelId: number): Promise<Channel> {
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
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

    async deleteChannel(channelId: number): Promise<void> {
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
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

    async getChannelById(channelId: number): Promise<Channel> {
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
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
}