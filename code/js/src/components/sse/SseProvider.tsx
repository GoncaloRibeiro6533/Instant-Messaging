import * as React from 'react'
import { createContext, useState, useEffect } from 'react'
import { useAuth } from '../auth/AuthProvider'
import { useData } from '../data/DataProvider'
import { Message } from '../../domain/Message'
import { User } from '../../domain/User'
import { Channel } from '../../domain/Channel'
import { Visibility } from '../../domain/Visibility'



type AppNotification = {
    id: number,
    message: string,
}

type SseContextType = {
    sse: EventSource | undefined,
    setSse: (sse: EventSource | null) => void
    notifications: AppNotification[],
}


export const SseContext = createContext<SseContextType>({
    sse: undefined,
    setSse: () => {},
    notifications: [],
})

export function SseProvider({ children }: { children: React.ReactNode }) : React.JSX.Element {
    const [sse, setSse] = useState<EventSource | undefined>(undefined)
    const [notifications, setNotifications] = useState<AppNotification[]>([])
    const [ user ] = useAuth();	
    const { addMessages, updateChannel} = useData()

    useEffect(() => {
        if(user === undefined) return
        const eventSource = new EventSource(`http://localhost:8080/api/sse/listen/${user.token}`);
        
        eventSource.addEventListener('NewChannelMessage', (event) => {
            const data  = JSON.parse(event.data)
            const message = messageMapper(data.message)
            addMessages(message.channel, [message]);
        })

        eventSource.addEventListener('ChannelNameUpdate', (event) => {
            const data  = JSON.parse(event.data)
            const channel = channelMapper(data)
            updateChannel(channel)
        })

        return () => {
            eventSource.close();
        };

    } , [user])


    return (
        <SseContext.Provider value={{ sse, setSse, notifications }}>
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
        state.notifications
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