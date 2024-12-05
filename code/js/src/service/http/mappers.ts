import { Channel } from "../../domain/Channel";
import { Role } from "../../domain/Role";
import { User } from "../../domain/User";
import { Message } from "../../domain/Message";
import { ChannelMember } from "../../domain/ChannelMember";
import { Visibility } from "../../domain/Visibility";
import {RegisterInvitation} from "../../domain/RegisterInvitation";
import {ChannelInvitation} from "../../domain/ChannelInvitation";

export async function channelMembersMapper(json: any): Promise<ChannelMember[]> {
    const members: ChannelMember[] = [];
    for (const member of json.members) {
        const role = Role[member.role as keyof typeof Role];
        const user = new User(Number(member.user.id), member.user.username, member.user.email);
        const channelMember = {user, role}
        members.push(channelMember);
    }
    return members;
}

export async function channelsListMapper(json: any): Promise<Map<Channel, Role>> {
    const channels: Map<Channel, Role> = new Map();
    for (const {channel, role} of json.channels) {
        const creator = new User(Number(channel.creator.id), channel.creator.username, channel.creator.email);
        const visibility = Visibility[channel.visibility as keyof typeof Visibility];
        const ch = new Channel(Number(channel.id), channel.name, creator, visibility);
        channels.set(ch, Role[role as keyof typeof Role]);
    }
    return channels;
}

export async function messagesMapper(
    json: any
): Promise<Message[]> {
    const messages: Message[] = [];
    const creator = new User(Number(json.channel.creator.id), json.channel.creator.username, json.channel.creator.email);
    const visibility = Visibility[json.channel.visibility as keyof typeof Visibility];
    const ch = new Channel(Number(json.channel.id), json.channel.name, creator, visibility);
    for (const message of json.messages as any[]) {
        const user = new User(Number(message.sender.id), message.sender.username, message.sender.email);
        const date = new Date(message.timestamp);
        const msg = new Message(Number(message.msgId), user, ch, message.content, date);
        messages.push(msg);
    }
    return messages.reverse();
}


export async function registerInvitationMapper(json: any): Promise<RegisterInvitation> {
    const sender = new User(Number(json.sender.id), json.sender.username, json.sender.email);
    const creator = new User(Number(json.channel.creator.id), json.channel.creator.username, json.channel.creator.email);
    const channel = new Channel(Number(json.channel.id), json.channel.name, creator, Visibility[json.channel.visibility as keyof typeof Visibility]);
    const role = Role[json.role as keyof typeof Role];
    const timestamp = new Date(json.timestamp)
    return {
        id: json.id,
        sender,
        email: json.email,
        channel: channel,
        role,
        isUsed: json.isUsed,
        timestamp,
    }
}

export async function channelInvitationMapper(json: any): Promise<ChannelInvitation> {
    const sender = new User(Number(json.sender.id), json.sender.username, json.sender.email);
    const receiver = new User(Number(json.receiver.id), json.receiver.username, json.receiver.email);
    const creator = new User(Number(json.channel.creator.id), json.channel.creator.username, json.channel.creator.email);
    const channel =  new Channel(Number(json.channel.id), json.channel.name, creator, Visibility[json.channel.visibility as keyof typeof Visibility])
    const role = Role[json.role as keyof typeof Role];
    const timestamp = new Date(json.timestamp)
    return {
        id: json.id,
        sender,
        receiver,
        channel: channel,
        role,
        isUsed: json.isUsed,
        timestamp,
    }

}

