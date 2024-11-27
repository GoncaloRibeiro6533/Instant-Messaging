import { Channel } from "../../domain/Channel";
import { ChannelMember } from "../../domain/ChannelMember";
import { Role } from "../../domain/Role";
import { Repo } from "../../App";

export interface ChannelService {

    createChannel(token: string, name: string, visibility: string): Promise<Channel>;
    joinChannel(token: string, channelId: number, role: Role): Promise<Channel>;
    searchChannelByName(token: string, name: string, limit: number, skip: number): Promise<Channel[]>;
    getChannelMembers(token: string, channelId: number): Promise<ChannelMember[]>;
    getChannelsOfUser(token: string, userId: number): Promise<Channel[]>;
    updateChannelName(token: string, channelId: number, newName: string): Promise<Channel>;
    leaveChannel(token: string, channelId: number): Promise<Channel>;
}

