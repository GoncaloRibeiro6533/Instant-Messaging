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
    searchChannelByName(name: string, limit: number, skip: number): Channel[];
    getChannelMembers(user: User, channelId: number): ChannelMember[];
    getChannelsOfUser(user: User, userId: number): Map<Channel,Role>;
    updateChannelName(user: User, channelId: number, newName: string): Channel;
    leaveChannel(user: User, channelId: number): Channel;
    getChannelById(channelId: number): Channel;
}

export class ChannelRepo implements ChannelRepoInterface {
    nextId: number = 11;
    public channels: Channel[] = [
        new Channel(1, 'Channel 1', new User(1, 'Bob', 'bob@example.com'), Visibility.PUBLIC),
        new Channel(2, 'Channel 2', new User(1, 'Bob', 'bob@example.com'), Visibility.PRIVATE),
        new Channel(3, 'Channel 3', new User(1, 'Bob', 'bob@example.com'), Visibility.PUBLIC),
    ];

    public channelMembers: Map<Channel, Array<ChannelMember>> = new Map([
        [this.channels[0], [
            {
                user: new User(1, 'Bob', 'bob@example.com'),
                role: Role.READ_WRITE
            },
            {
                user: new User(2, 'Alice', 'alice@email.com'),
                role: Role.READ_ONLY
            },
            {
                user: new User(3, 'Charles', 'charles@email.com'),
                role: Role.READ_WRITE
            }
        ]],
        [this.channels[1], [
            {
                user: new User(1, 'Bob', 'bob@example.com'),
                role: Role.READ_WRITE
            }
        ]],
        [this.channels[2], [
            {
                    user: new User(1, 'Bob', 'bob@example.com'),
                    role: Role.READ_ONLY
                }
        ]]
    ])

    createChannel(user: User, name: string, visibility: string): Channel {
        const channel: Channel = new Channel( this.nextId++,
            name,
            user,
            Visibility[visibility as keyof typeof Visibility]
        );
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

    searchChannelByName(name: string, limit: number, skip: number): Channel[] {
        return this.channels.filter(channel => channel.name.includes(name)).slice(skip, skip + limit);
    }

    getChannelMembers(user: User, channelId: number): ChannelMember[] {
        const channel = this.channels.find(channel => channel.id === channelId);
        return this.channelMembers.get(channel)!;
    }

    getChannelsOfUser(user: User, userId: number): Map<Channel,Role> {
        const userChannels: Channel[] = [];
        this.channelMembers.forEach((members, channel) => {
            if (members.find(member => member.user.id === userId)) {
                userChannels.push(channel);
            }
        })
        const userChannelsWithRole: Map<Channel,Role> = new Map();
        userChannels.forEach(channel => {
            const member = this.channelMembers.get(channel)!.find(member => member.user.id === userId);
            userChannelsWithRole.set(channel, member!.role);
        });
        return userChannelsWithRole;
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