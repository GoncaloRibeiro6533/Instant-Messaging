import { ChannelService } from "../interfaces/ChannelService";
import { Channel } from "../../domain/Channel";
import { ChannelMember } from "../../domain/ChannelMember";
import { Role } from "../../domain/Role";

export class ChannelServiceHttp implements ChannelService {
    private baseUrl = 'http://localhost:8080/api/channels';

    async createChannel(token: string, channelName: string): Promise<Channel> {
        try {
            const response = await fetch(this.baseUrl, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ name: channelName }),
            });
            if (!response.ok) throw new Error('Failed to create channel');
            const channel = await response.json();
            return new Channel(channel.id, channel.name, channel.creator, channel.visibility);
        } catch (error) {
            console.error('Error creating channel:', error);
            throw error;
        }
    }

    async getChannelById(token: string, channelId: number): Promise<Channel> {
        try {
            const response = await fetch(`${this.baseUrl}/${channelId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });
            if (!response.ok) throw new Error('Channel not found');
            const data = await response.json();
            return new Channel(data.id, data.name, data.creator, data.visibility);
        } catch (error) {
            console.error('Error fetching channel:', error);
            throw error;
        }
    }

    async getChannelMembers(token: string, channelId: number): Promise<ChannelMember[]> {
        try {
            const response = await fetch(`${this.baseUrl}/${channelId}/members`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });
            if (!response.ok) throw new Error('Failed to get channel members');
            const data = await response.json();
            //return data.members.map(
            //    (member: any) => new ChannelMember(member.user, member.role)
            //);
        } catch (error) {
            console.error('Error fetching channel members:', error);
            throw error;
        }
        return Promise.resolve([]); //todo
    }

    async getChannelsOfUser(token: string, userId: number): Promise<Map<Channel, Role>> {
        try {
            const response = await fetch(`${this.baseUrl}/user/${userId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });
            if (!response.ok) throw new Error('Failed to get channels of user');
            const data = await response.json();
            const channelMap = new Map<Channel, Role>();
            data.channels.forEach((entry: any) => {
                const channel = new Channel(
                    entry.channel.id,
                    entry.channel.name,
                    entry.channel.creator,
                    entry.channel.visibility
                );
                channelMap.set(channel, entry.role);
            });
            return channelMap;
        } catch (error) {
            console.error('Error fetching channels of user:', error);
            throw error;
        }
    }

    async joinChannel(token: string, channelId: number, role: Role): Promise<Channel> {
        try {
            const response = await fetch(`${this.baseUrl}/${channelId}/add/${role}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });
            if (!response.ok) throw new Error('Failed to join channel');
            const data = await response.json();
            return new Channel(data.id, data.name, data.creator, data.visibility);
        } catch (error) {
            console.error('Error joining channel:', error);
            throw error;
        }
    }

    async leaveChannel(token: string, channelId: number): Promise<Channel> {
        /* todo add parameter userId
        try {
            const response = await fetch(`${this.baseUrl}/${channelId}/leave/${userId}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });
            if (!response.ok) throw new Error('Failed to leave channel');
            const data = await response.json();
            return new Channel(data.id, data.name, data.creator, data.visibility);
        } catch (error) {
            console.error('Error leaving channel:', error);
            throw error;
        }
         */
        return Promise.resolve(undefined);
    }

    async searchChannelByName(token: string, name: string, limit: number, skip: number): Promise<Channel[]> {
        try {
            const response = await fetch(`${this.baseUrl}/search/${name}?limit=${limit}&skip=${skip}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });
            if (!response.ok) throw new Error('Failed to search channels by name');
            const data = await response.json();
            return data.channels.map(
                (channel: any) => new Channel(channel.id, channel.name, channel.creator, channel.visibility)
            );
        } catch (error) {
            console.error('Error searching channel by name:', error);
            throw error;
        }
    }

    async updateChannelName(token: string, channelId: number, newName: string): Promise<Channel> {
        try {
            const response = await fetch(`${this.baseUrl}/${channelId}/${newName}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });
            if (!response.ok) throw new Error('Failed to update channel name');
            const data = await response.json();
            return new Channel(data.id, data.name, data.creator, data.visibility);
        } catch (error) {
            console.error('Error updating channel name:', error);
            throw error;
        }
    }
}
