import { Channel } from "../../domain/Channel";
import { ChannelMember } from "../../domain/ChannelMember";
import { Role } from "../../domain/Role";

export interface ChannelService {

    createChannel(name: string, visibility: string): Promise<Channel>;
    joinChannel(channelId: number, role: Role): Promise<Channel>;
    searchChannelByName(name: string, limit: number, skip: number): Promise<Channel[]>;
    getChannelMembers(channelId: number): Promise<ChannelMember[]>;
    getChannelsOfUser(userId: number): Promise<Map<Channel,Role>>;
    updateChannelName(channelId: number, newName: string): Promise<Channel>;
    getChannelById(channelId: number): Promise<Channel>;
    leaveChannel(channelId: number): Promise<Channel>;
}

