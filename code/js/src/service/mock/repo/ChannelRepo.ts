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
            visibility: Visibility.PRIVATE
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
        },
        {
            id: 4,
            name: 'Channel 4',
            creator: {
                id: 3,
                username: 'Charlie',
                email: 'charl@email.com'
            },
            visibility: Visibility.PRIVATE
        },

        {
            id: 5,
            name: 'Channel 5',
            creator: {
                id: 3,
                username: 'Charlie',
                email: 'charl@email.com'
            },
            visibility: Visibility.PRIVATE
        },
        {
            id: 6,
            name: 'Channel 6',
            creator: {
                id: 3,
                username: 'Charlie',
                email: 'charl@email.com'
            },
            visibility: Visibility.PRIVATE
        },
        {
            id: 7,
            name: 'Channel 7',
            creator: {
                id: 3,
                username: 'Charlie',
                email: 'charl@email.com'
            },
            visibility: Visibility.PRIVATE
        },
        {
            id: 8,
            name: 'Channel 8',
            creator: {
                id: 3,
                username: 'Charlie',
                email: 'charl@email.com'
            },
            visibility: Visibility.PRIVATE
        },
        {
            id: 9,
            name: 'Channel 9',
            creator: {
                id: 3,
                username: 'Charlie',
                email: 'charl@email.com'
            },
            visibility: Visibility.PRIVATE
        },
        {
            id: 10,
            name: 'Channel 10',
            creator: {
                id: 3,
                username: 'Charlie',
                email: 'charl@email.com'
            },
            visibility: Visibility.PRIVATE
        },
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
                },
            ]
        ],
        [
            this.channels[1],
            [
                {
                    user: {
                        id: 1,
                        username: 'Bob',
                        email: 'bob@example.com'
                    },
                    role: Role.READ_ONLY
                },
            ]
        ], [
            this.channels[2],
            [
                {
                    user: {
                        id: 1,
                        username: 'Bob',
                        email: 'bob@example.com'
                    },
                    role: Role.READ_ONLY
                },
            ]
        ], [
            this.channels[3],
            [
                {
                    user: {
                        id: 1,
                        username: 'Bob',
                        email: 'bob@example.com'
                    },
                    role: Role.READ_ONLY
                },
            ]
        ],
        [
            this.channels[4],
            [
                {
                    user: {
                        id: 1,
                        username: 'Bob',
                        email: 'bob@example.com'
                    },
                    role: Role.READ_ONLY
                },
            ]
        ],
        [
            this.channels[5],
            [
                {
                    user: {
                        id: 1,
                        username: 'Bob',
                        email: 'bob@example.com'
                    },
                    role: Role.READ_ONLY
                },
            ]
        ],
        [
            this.channels[6],
            [
                {
                    user: {
                        id: 1,
                        username: 'Bob',
                        email: 'bob@example.com'
                    },
                    role: Role.READ_ONLY
                },
            ]
        ],
        [
            this.channels[7],
            [
                {
                    user: {
                        id: 1,
                        username: 'Bob',
                        email: 'bob@example.com'
                    },
                    role: Role.READ_ONLY
                },
            ]
        ],
        [
            this.channels[8],
            [
                {
                    user: {
                        id: 1,
                        username: 'Bob',
                        email: 'bob@example.com'
                    },
                    role: Role.READ_ONLY
                },
            ]
        ],
        [
            this.channels[9],
            [
                {
                    user: {
                        id: 1,
                        username: 'Bob',
                        email: 'bob@example.com'
                    },
                    role: Role.READ_ONLY
                },
            ]
        ],
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