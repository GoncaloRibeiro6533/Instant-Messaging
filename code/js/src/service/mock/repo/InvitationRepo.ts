import {ChannelInvitation} from "../../../domain/ChannelInvitation";
import {RegisterInvitation} from "../../../domain/RegisterInvitation";
import {Channel} from "../../../domain/Channel";
import {Role} from "../../../domain/Role";
import {User} from "../../../domain/User";
import {Visibility} from "../../../domain/Visibility";
import {userRepo} from "./UserRepo";

interface InvitationRepoInterface {
    getInvitationsOfUser(user: User): Promise<Array<ChannelInvitation>>;
    getRegisterInvitationById(user: User,invitationId: number): Promise<RegisterInvitation>;
    createRegisterInvitation(user: User, email: string, channel: Channel, role: Role,): Promise<RegisterInvitation>;
    createChannelInvitation(user: User, receiverId: number, channel: Channel, role: Role): Promise<ChannelInvitation>;

    acceptChannelInvitation(user: User, invitationId: number): Promise<Channel>;

    declineChannelInvitation(user: User, invitationId: number): Promise<Boolean>;
}

export class InvitationRepo implements InvitationRepoInterface {
    invitations: Array<ChannelInvitation>;

    public registerInvitations = [
        {
            id: 1,
            sender: new User(1, 'Bob', 'bob@example.com'),
            email: 'david@example.com',
            channel: new Channel(
                1,
                'Channel 1',
                new User(1, 'Bob', 'bob@example.com'),
                Visibility.PUBLIC
            ),
            role: Role.READ_ONLY,
            isUsed: false,
            timestamp: new Date()
        },
        {
            id: 2,
            sender: new User(1, 'Bob', 'bob@example.com'),
            email: 'eva@example.com',
            channel:new Channel(
                    1,
                'Channel 1',
                new User(1, 'Bob', 'bob@example.com'),
                Visibility.PUBLIC
            ),
            role: Role.READ_WRITE,
            isUsed: false,
            timestamp: new Date()
        },
        {
            id: 3,
            sender: new User(2, 'Alice', 'alice@example.com'),
            channel: new Channel(
                2,
            'Channel 2',
            new User(2, 'Alice', 'alice@example.com'),
            Visibility.PRIVATE
        ),
            email: 'filipe@example.com',
            role: Role.READ_WRITE,
            isUsed: false,
            timestamp: new Date()
        },
        ]

    constructor() {
        this.invitations = [
            {
                id: 1,
                sender: new User(2, 'Alice', 'alice@example.com'),
                receiver: new User(1, 'Bob', 'bob@example.com'),
                channel: new Channel(4, 'Channel 4', new User(2, 'Alice', 'alice@example.com'), Visibility.PRIVATE),
                role: Role.READ_WRITE,
                isUsed: false,
                timestamp: new Date()
            },
            {
                id: 2,
                sender: new User(2, 'Alice', 'alice@example.com'),
                receiver: new User(1, 'Bob', 'bob@example.com'),
                channel: new Channel(5, 'Movies and Books', new User(2, 'Alice', 'alice@example.com'), Visibility.PUBLIC),
                role: Role.READ_ONLY,
                isUsed: false,
                timestamp: new Date()
            }
        ];
        this.registerInvitations = [];
    }

    async getInvitationsOfUser(user: User): Promise<Array<ChannelInvitation>> {
        return this.invitations.filter(invitation => invitation.receiver.id === user.id);
    }

    async getRegisterInvitationById(user: User,invitationId: number): Promise<RegisterInvitation> {
        return this.registerInvitations.find(invitation => invitation.id === invitationId);
    }

    async createRegisterInvitation(user: User, email: string, channel: Channel, role: Role,): Promise<RegisterInvitation> {
        const invitation = {
            id: this.registerInvitations.length + 1,
            sender: user,
            email: email,
            channel: channel,
            role: role,
            isUsed: false,
            timestamp: new Date()
        }
        this.registerInvitations.push(invitation);
        return invitation;
    }

    async createChannelInvitation(user: User, receiverId: number, channel: Channel, role: Role): Promise<ChannelInvitation> {
        const invitation = {
            id: this.invitations.length + 1,
            sender: user,
            receiver: userRepo.getUserById(receiverId),
            channel: channel,
            role: role,
            isUsed: false,
            timestamp: new Date()
        }
        this.invitations.push(invitation);
        return invitation;
    }

    async acceptChannelInvitation(user: User, invitationId: number): Promise<Channel> {
        const invitation = this.invitations.find(invitation => invitation.id === invitationId);
        if (invitation.receiver.id !== user.id) {
            throw new Error("Invalid user");
        }
        invitation.isUsed = true;
        return invitation.channel;
    }

    async declineChannelInvitation(user: User, invitationId: number): Promise<Boolean> {
        const invitation = this.invitations.find(invitation => invitation.id === invitationId);
        if (invitation.receiver.id !== user.id) {
            throw new Error("Invalid user");
        }
        this.invitations = this.invitations.filter(invitation => invitation.id !== invitationId);
        return true;
    }
}