import * as React from 'react'
import { createContext, useState, useEffect } from 'react'
import { useAuth } from '../auth/AuthProvider'
import { useData } from '../data/DataProvider'
import { Message } from '../../domain/Message'
import { User } from '../../domain/User'
import { Channel } from '../../domain/Channel'
import { Visibility } from '../../domain/Visibility'
import { ChannelMember } from '../../domain/ChannelMember'
import { Role } from '../../domain/Role'
import { ChannelInvitation } from '../../domain/ChannelInvitation'

type AppNotification = {
    id: number,
    message: string,
}

type SseContextType = {
    sse: EventSource | undefined,
    setSse: (sse: EventSource | null) => void
    notifications: AppNotification[],
    deleteNotification: (id: number) => void
}

export const SseContext = createContext<SseContextType>({
    sse: undefined,
    setSse: () => {},
    notifications: [],
    deleteNotification: () => {}
})

export function SseProvider({ children }: { children: React.ReactNode }) : React.JSX.Element {
    const [sse, setSse] = useState<EventSource | undefined>(undefined)
    const [notifications, setNotifications] = useState<AppNotification[]>([])
    const [ user ] = useAuth();
    const {
        addMessages,
        updateChannel,
        removeChannelMember,
        addChannelMember,
        addInvitation,
    } = useData()

    function addNotification(id:number, message: string) {
        setNotifications([...notifications, { id, message }])
    }
    function deleteNotification(id: number) {
        setNotifications(
            notifications.filter(notification => notification.id !== id)
        )
    }

    useEffect(() => {
        if(user === undefined) return
        const eventSource = new EventSource(`http://localhost:8080/api/sse/listen`,{
            withCredentials: true,
        });

        eventSource.addEventListener('NewChannelMessage', (event) => {
            const data  = JSON.parse(event.data)
            const message = messageMapper(data.message)
            addMessages(message.channel, [message]);
        })

        eventSource.addEventListener('ChannelNameUpdate', (event) => {
            const data  = JSON.parse(event.data)
            const channel = channelMapper(data)
            updateChannel(channel)
            addNotification(data.id, `Channel ${channel.name} has been updated`)
        })

        eventSource.addEventListener('NewMemberUpdate', (event) => {
            const data  = JSON.parse(event.data)
            const removedMeber = userMapper(data.removedMember)
            const channel = channelMapper(data.channel)
            removeChannelMember(channel.id, removedMeber)
        })

        eventSource.addEventListener('ChannelNewMemberUpdate', (event) => {
            const data  = JSON.parse(event.data)
            const channel = channelMapper(data.channel)
            const newMember = memberMapper(data.newMember, data.role)
            addChannelMember(channel.id, [newMember])
        })

        eventSource.addEventListener('ChannelMemberExitedUpdate', (event) => {
            const data  = JSON.parse(event.data)
            const channel = channelMapper(data.channel)
            const removedMember = userMapper(data.removedMember)
            removeChannelMember(channel.id, removedMember)
        })

        eventSource.addEventListener('NewInvitationUpdate', (event) => {
            const data  = JSON.parse(event.data)
            const invitation = invitationMapper(data)
            addInvitation(invitation)
            addNotification(data.id, `You have been invited to join channel ${invitation.channel.name}`)
        })

        eventSource.onerror = () => {
            console.error("SSE connection error");
            eventSource.close();
        };


        return () => {
            eventSource.close();
        };

    } , [user])

    return (
        <SseContext.Provider value={{ sse, setSse, notifications, deleteNotification }}>
            {children}
        </SseContext.Provider>
    )
}


export function useSse() {
    const state =  React.useContext(SseContext)
    return [
        state.sse,
        (sse: EventSource | null) => {
            if (sse) {
                state.setSse(sse)
            } else {
                state.setSse(null)
            }
        },
        state.notifications,
        state.deleteNotification
    ] as const
}

function messageMapper(json: any): Message {
    const creator = new User(Number(json.channel.creator.id), json.channel.creator.username, json.channel.creator.email);
    const visibility = Visibility[json.channel.visibility as keyof typeof Visibility];
    const ch = new Channel(Number(json.channel.id), json.channel.name, creator, visibility);
    const user = new User(Number(json.sender.id), json.sender.username, json.sender.email);
    const date = new Date(json.timestamp);
    const msg = new Message(Number(json.id), user, ch, json.content, date);
    return msg
}

function channelMapper(json: any): Channel {
    const creator = new User(Number(json.creator.id), json.creator.username, json.creator.email);
    const visibility = Visibility[json.visibility as keyof typeof Visibility];
    const ch = new Channel(Number(json.id), json.name, creator, visibility);
    return ch
}

function userMapper(json: any): User {
    return new User(Number(json.id), json.username, json.email)
}

function memberMapper(user: any, role: any): ChannelMember {
    return {
        user: userMapper(user),
        role: Role[role as keyof typeof Role]
    }
}

function invitationMapper(json: any): ChannelInvitation {
    const sender = userMapper(json.sender)
    const receiver = userMapper(json.receiver)
    const channel = channelMapper(json.channel)
    const role = Role[json.role as keyof typeof Role]
    const timestamp = new Date(json.timestamp)
    return {
        id: json.id,
        sender,
        receiver,
        channel,
        role,
        isUsed: json.isUsed,
        timestamp
    } as const
}