import { Channel } from "../../domain/Channel";
import { ChannelMember } from "../../domain/ChannelMember";
import { Role } from "../../domain/Role";

export interface ChannelService {

    createChannel(token: string, name: string, visibility: string): Promise<Channel>;
    joinChannel(token: string, channelId: number, role: Role): Promise<Channel>;
    searchChannelByName(token: string, name: string, limit: number, skip: number): Promise<Channel[]>;
    getChannelMembers(token: string, channelId: number): Promise<ChannelMember[]>;
    getChannelsOfUser(token: string, userId: number): Promise<Map<Channel,Role>>;
    updateChannelName(token: string, channelId: number, newName: string): Promise<Channel>;
    getChannelById(token: string, channelId: number): Promise<Channel>;
    leaveChannel(token: string, channelId: number): Promise<Channel>;
}

