import { Channel } from '../../../domain/Channel';
import { Visibility } from '../../../domain/Visibility';
import { Role } from '../../../domain/Role';
import { User } from '../../../domain/User';
import { ChannelMember } from '../../../domain/ChannelMember';


interface ChannelRepoInterface {
    channels: Array<Channel>;
    channelMembers: Map<Channel, Array<ChannelMember>>;
    nextId: number;
    createChannel(user: User, name: string, visibility: string): Channel;
    joinChannel(user: User, channelId: number, role: Role): Channel;
    searchChannelByName(user: User, name: string, limit: number, skip: number): Channel[];
    getChannelMembers(user: User, channelId: number): ChannelMember[];
    getChannelsOfUser(user: User, userId: number): Channel[];
    updateChannelName(user: User, channelId: number, newName: string): Channel;
    leaveChannel(user: User, channelId: number): Channel;
    getChannelById(channelId: number): Channel;
}

export class ChannelRepo implements ChannelRepoInterface {
    nextId: number = 2;
    public channels: Channel[] = [
        {
            id: 1,
            name: 'Channel 1',
            creator: {
                id: 1,
                username: 'Bob',
                email: 'bob@example.com'
            },
            visibility: Visibility.PUBLIC
        },
        {
            id: 2,
            name: 'Channel 2',
            creator: {
                id: 2,
                username: 'Alice',
                email: 'alice@email.com'
            },
            visibility: Visibility.PUBLIC
        },
        {
            id: 3,
            name: 'Channel 3',
            creator: {
                id: 3,
                username: 'Charlie',
                email: 'charl@email.com'
            },
            visibility: Visibility.PRIVATE
        }
    ]

    public channelMembers: Map<Channel, Array<ChannelMember>> = new Map([
        [
            this.channels[0],
            [
                {
                    user: {
                        id: 1,
                        username: 'Bob',
                        email: 'bob@example.com'
                    },
                    role: Role.READ_WRITE
                },
                {
                    user: {
                        id: 2,
                        username: 'Alice',
                        email: 'alice@email.com'
                    },
                    role: Role.READ_WRITE
                }
            ]
        ]
    ]);

    createChannel(user: User, name: string, visibility: string): Channel {
        const channel: Channel = {
            id: this.nextId++,
            name,
            creator: user,
            visibility: Visibility[visibility as keyof typeof Visibility]
        };
        this.channels.push(channel);
        this.channelMembers.set(channel, [{
            user,
            role: Role.READ_WRITE
        }]);
        return channel;
    }

    joinChannel(user: User, channelId: number, role: Role): Channel {
        const channel = this.channels.find(channel => channel.id === channelId);
        const channelMember: ChannelMember = {
            user,
            role
        };
        this.channelMembers.get(channel)!.push(channelMember);
        return channel;
    }

    searchChannelByName(user: User, name: string, limit: number, skip: number): Channel[] {
        return this.channels.filter(channel => channel.name.includes(name)).slice(skip, skip + limit);
    }

    getChannelMembers(user: User, channelId: number): ChannelMember[] {
        const channel = this.channels.find(channel => channel.id === channelId);
        return this.channelMembers.get(channel)!;
    }

    getChannelsOfUser(user: User, userId: number): Channel[] {
        return this.channels.filter(channel => this.channelMembers.get(channel)!.find(member => member.user.id === userId));
    }

    updateChannelName(user: User, channelId: number, newName: string): Channel {
        const channel = this.channels.find(channel => channel.id === channelId);
        channel.name = newName;
        return channel;
    }

    leaveChannel(user: User, channelId: number): Channel {
        const channel = this.channels.find(channel => channel.id === channelId);
        const channelMemberIndex = this.channelMembers.get(channel)!.findIndex(member => member.user.id === user.id);
        this.channelMembers.get(channel)!.splice(channelMemberIndex, 1);
        return channel;
    }

    deleteChannel(user: User, channelId: number): void {
        const channelIndex = this.channels.findIndex(channel => channel.id === channelId);
        this.channels.splice(channelIndex, 1);
        this.channelMembers.delete(this.channels[channelIndex]);
    }

    getChannelById(channelId: number): Channel {
        return this.channels.find(channel => channel.id === channelId)!;
    }
}