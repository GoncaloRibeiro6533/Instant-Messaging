import {Channel} from "../../domain/Channel"
import {Role} from "../../domain/Role"
import {Message} from "../../domain/Message"
import {User} from "../../domain/User"
import {ChannelInvitation} from "../../domain/ChannelInvitation"
import * as React from "react"
import {ChannelMember} from "../../domain/ChannelMember"

const CHANNELS_KEY = 'channels'
const MESSAGES_KEY = 'messages'
const INVITATIONS_KEY = 'invitations'

type DataContextType = {
    channels: Map<Number, Channel>,
    roles : Map<Number,Role>,
    messages : Map<Number,Message[]>,
    invitations: Array<ChannelInvitation>,
    setChannels: (channels : Map<Number,Channel>) => void,
    updateChannel: (channel : Channel) => void
    setMessages: (messages : Map<Number,Message[]>) => void,
    setInvitations: (invitations : Array<ChannelInvitation>) => void,
    addChannel: (channel : Channel, role : Role) => void,
    removeChannel: (channel : Channel) => void,
    addMessages: (channel : Channel, messages : Message[]) => void,
    loadMessages: (channel : Channel, messages: Message[]) => void,
    addInvitation: (invitation : ChannelInvitation) => void,
    removeInvitation: (invitation : ChannelInvitation) => void,
    channelMembers: Map<Number, ChannelMember[]> // <channelId, User>
    addChannelMember: (channelId : Number, user : ChannelMember[]) => void,
    removeChannelMember: (channelId : Number, user:User) => void,
    setChannelMembers: (channelMembers : Map<Number, ChannelMember[]>) => void,
    addChannels: (channels : Map<Channel,Role>) => void,
    orderByMessages: () => void,
    clear : () => void
}

export const DataContext = React.createContext<DataContextType>({
    channels: new Map(),
    roles: new Map(),
    messages: new Map(),
    invitations: [],
    setChannels: () => {},
    updateChannel: () => {},
    setMessages: () => {},
    setInvitations: () => {},
    addChannel: () => {},
    removeChannel: () => {},
    addMessages: () => {},
    loadMessages: () => {},
    addInvitation: () => {},
    removeInvitation: () => {},
    channelMembers: new Map(),
    addChannelMember: () => {},
    removeChannelMember: () => {},
    setChannelMembers: () => {},
    addChannels: () => {},
    orderByMessages: () => {},
    clear : () => {}
})

export function DataProvider({ children }: { children: React.ReactNode }) : React.JSX.Element {

    //Initial state
    const initialChannels = new Map()
    const initialRoles = new Map()
    const initialMessages =  new Map()
    const initialInvitations: ChannelInvitation[] = []
    const initialChannelMembers = new Map<Number, ChannelMember[]>()
    //Hooks
    const [channels, setChannels] = React.useState<Map<Number,Channel>>(initialChannels)
    const [roles, setRoles] = React.useState<Map<Number,Role>>(initialRoles)
    const [messages, setMessages] = React.useState<Map<Number,Message[]>>(initialMessages)
    const [invitations, setInvitations] = React.useState<Array<ChannelInvitation>>(initialInvitations)
    const [channelMembers, setChannelMembers] = React.useState<Map<Number, ChannelMember[]>>(initialChannelMembers)

    const addChannel = (channel : Channel, role : Role) => {
        setChannels((prevChannels) => {
            const newChannels = new Map(prevChannels)
            newChannels.set(channel.id, channel)
            return newChannels
        })
        setRoles((prevRoles) => {
            const newRoles = new Map(prevRoles)
            newRoles.set(channel.id, role)
            return newRoles
        })
    }
    const removeChannel = (channel : Channel) => {
        setChannels((prevChannels) => {
            const newChannels = new Map(prevChannels)
            newChannels.delete(channel.id)
            return newChannels
        })
        setRoles((prevRoles) => {
            const newRoles = new Map(prevRoles)
            newRoles.delete(channel.id)
            return newRoles
        })

        setMessages((prevMessages) => {
            const newMessages = new Map(prevMessages)
            newMessages.delete(channel.id)
            return newMessages
        })
    }

    const updateChannel = (channel : Channel) => {
        setChannels((prevChannels) => {
            const newChannels = new Map(prevChannels)
            newChannels.set(channel.id, channel)
            return newChannels
        })
    }
    const addMessages = (channel : Channel, messagesToAdd : Message[]) => {
        setMessages((prevMessages) => {
            const newMessages = new Map(prevMessages)
            const channelMessages = newMessages.get(channel.id) || []
            newMessages.set(channel.id, [...messagesToAdd, ...channelMessages])
            return newMessages
        })
    }

    const loadMessages = (channel : Channel, messagesToAdd : Message[]) => {
        setMessages((prevMessages) => {
            const newMessages = new Map(prevMessages)
            const channelMessages = newMessages.get(channel.id) || []
            const newMessagesToAdd = [...channelMessages, ...messagesToAdd]
            newMessages.set(channel.id, newMessagesToAdd)
            return newMessages
        })
    }
    const addInvitation = (invitation : ChannelInvitation) => {
        setInvitations((prevInvitations) => {
            const newInvitations = [...prevInvitations, invitation]
            return newInvitations
        })
    }

    const removeInvitation = (invitation : ChannelInvitation) => {
        setInvitations((prevInvitations) => {
            const newInvitations = [...prevInvitations]
            const index = newInvitations.indexOf(invitation)
            newInvitations.splice(index,1)
            return newInvitations
        })
    }

    const addChannelMember = (channelId : Number, user : ChannelMember[]) => {
        setChannelMembers((prevChannelMembers) => {
            const newChannelMembers = new Map(prevChannelMembers)
            const users = newChannelMembers.get(channelId) || []
            newChannelMembers.set(channelId, [...users, ...user])
            return newChannelMembers
        })
    }

    const removeChannelMember = (channelId : Number, user: User) => {
        setChannelMembers((prevChannelMembers) => {
            const newChannelMembers = new Map(prevChannelMembers)
            const users = newChannelMembers.get(channelId) || []
            const index = users.findIndex((u) => u.user.id === user.id)
            users.splice(index,1)
            newChannelMembers.set(channelId, users)
            return newChannelMembers
        })
    }

    const addChannels = (channels : Map<Channel,Role>) => {
        setChannels((prevChannels) => {
            const newChannels = new Map(prevChannels)
            Array.from(channels.keys()).forEach((channel) => {
                newChannels.set(channel.id, channel)
            })
            return newChannels
        })
        setRoles((prevRoles) => {
            const newRoles = new Map(prevRoles)
            Array.from(channels.keys()).forEach((channel) => {
                newRoles.set(channel.id, channels.get(channel))
            })
            return newRoles
        })
    }

    const orderByMessages = () => {
        setChannels((prevChannels) => {
            const newChannels = new Map([...prevChannels.entries()].sort((a, b) => {
                const messagesA = messages.get(a[0]) || []
                const messagesB = messages.get(b[0]) || []
                return messagesB.length - messagesA.length
            }))
            return newChannels
    })}


    const clear = () => {
        setChannels(new Map())
        setMessages(new Map())
        setInvitations([])
    }
    
    return (
        <DataContext.Provider value={{
            channels,
            roles,
            messages,
            invitations,
            setChannels,
            updateChannel,
            setMessages,
            setInvitations,
            addChannel,
            removeChannel,
            addMessages,
            addInvitation,
            removeInvitation,
            clear,
            channelMembers,
            addChannelMember,
            removeChannelMember,
            loadMessages,
            setChannelMembers,
            addChannels,
            orderByMessages
        }}>
            {children}
        </DataContext.Provider>
    )
}

export function useData() {
    const state =  React.useContext(DataContext)
    return {
        channels: state.channels,
        roles: state.roles,
        messages: state.messages,
        invitations: state.invitations,
        setChannels: (channels: Map<Number,Channel>) => {
            state.setChannels(channels)
        },
        updateChannel: state.updateChannel,
        setMessages: (messages: Map<Number,Message[]>) => {
            state.setMessages(messages)
        },
        setInvitations: (invitations: Array<ChannelInvitation>) => {
            state.setInvitations(invitations)
        },
        setChannelMembers: (channelMembers: Map<Number, ChannelMember[]>) => {
            state.setChannelMembers(channelMembers)
        },
        addChannel: state.addChannel,
        removeChannel: state.removeChannel,
        addMessages: state.addMessages,
        addInvitation: state.addInvitation,
        removeInvitation: state.removeInvitation,
        channelMembers: state.channelMembers,
        addChannelMember: state.addChannelMember,
        removeChannelMember: state.removeChannelMember,
        clear: state.clear,
        loadMessages: state.loadMessages,
        addChannels: state.addChannels,
        orderByMessages: state.orderByMessages
    }

}

