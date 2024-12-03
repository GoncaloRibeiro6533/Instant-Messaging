import { Channel } from "../../domain/Channel"
import { Role } from "../../domain/Role"
import { Message } from "../../domain/Message"
import { ChannelInvitation } from "../../domain/ChannelInvitation"
import * as React from "react"

const CHANNELS_KEY = 'channels';
const MESSAGES_KEY = 'messages';
const INVITATIONS_KEY = 'invitations';

type DataContextType = {
    channels : Map<Channel,Role>,
    messages : Map<Number,Message[]>,
    invitations: Array<ChannelInvitation>,
    setChannels: (channels : Map<Channel,Role>) => void,
    updateChannel: (channel : Channel) => void
    setMessages: (messages : Map<Number,Message[]>) => void,
    setInvitations: (invitations : Array<ChannelInvitation>) => void,
    addChannel: (channel : Channel, role : Role) => void,
    removeChannel: (channel : Channel) => void,
    addMessages: (channel : Channel, messages : Message[]) => void,
    addInvitation: (invitation : ChannelInvitation) => void,
    removeInvitation: (invitation : ChannelInvitation) => void,
    clear : () => void
}

export const DataContext = React.createContext<DataContextType>({
    channels: new Map(),
    messages: new Map(),
    invitations: [],
    setChannels: () => {},
    updateChannel: () => {},
    setMessages: () => {},
    setInvitations: () => {},
    addChannel: () => {},
    removeChannel: () => {},
    addMessages: () => {},
    addInvitation: () => {},
    removeInvitation: () => {},
    clear : () => {}
})

export function DataProvider({ children }: { children: React.ReactNode }) : React.JSX.Element {
   
    //Initial state
    const initialChannels = new Map();
    const initialMessages =  new Map();
    const initialInvitations: ChannelInvitation[] = []
    //Hooks
    const [channels, setChannels] = React.useState<Map<Channel,Role>>(initialChannels);
    const [messages, setMessages] = React.useState<Map<Number,Message[]>>(initialMessages);
    const [invitations, setInvitations] = React.useState<Array<ChannelInvitation>>(initialInvitations);

    const addChannel = (channel : Channel, role : Role) => {
        const newChannels = new Map([...channels]);
        newChannels.set(channel,role);
        setChannels(newChannels);
    }
    const removeChannel = (channel : Channel) => {
        const newChannels = new Map([...channels]);
        newChannels.delete(channel);
        setChannels(new Map(newChannels));
        const newMessages = new Map([...messages]);
        newMessages.delete(channel.id);
        setMessages(newMessages);
    }

    const updateChannel = (channel : Channel) => {
        const newChannels = new Map([...channels]);
        const role = newChannels.get(channel);
        newChannels.delete(channel);
        newChannels.set(channel,role);
        setChannels(newChannels);
    }
    const addMessages = (channel : Channel, messagesToAdd : Message[]) => {
        const newMessages = new Map(messages); 
        const channelMessages = newMessages.get(channel.id) || []; 
        newMessages.set(channel.id, [...messagesToAdd, ...channelMessages]); 
        setMessages(newMessages);
    }
    const addInvitation = (invitation : ChannelInvitation) => {
        const newInvitations = [...invitations];
        newInvitations.push(invitation);
        setInvitations(newInvitations);
        localStorage.setItem(INVITATIONS_KEY,JSON.stringify([...newInvitations]));
    }
    const removeInvitation = (invitation : ChannelInvitation) => {
        const newInvitations = [...invitations];
        const index = newInvitations.indexOf(invitation);
        newInvitations.splice(index,1);
        setInvitations(newInvitations);
        localStorage.setItem(INVITATIONS_KEY,JSON.stringify([...newInvitations]));
    }


    const clear = () => {
        setChannels(new Map());
        setMessages(new Map());
        setInvitations([]);
        localStorage.removeItem(CHANNELS_KEY);
        localStorage.removeItem(MESSAGES_KEY);
        localStorage.removeItem(INVITATIONS_KEY);
    }
    return (
        <DataContext.Provider value={{
            channels,
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

            }}>
            {children}
        </DataContext.Provider>
    )
}

export function useData() {
    const state =  React.useContext(DataContext)
    return {
        channels: state.channels,
        messages: state.messages,
        invitations: state.invitations,
        setChannels: (channels: Map<Channel,Role>) => {
            state.setChannels(channels);
        },
        updateChannel: state.updateChannel,
        setMessages: (messages: Map<Number,Message[]>) => {
            state.setMessages(messages);
        },
        setInvitations: (invitations: Array<ChannelInvitation>) => {
            state.setInvitations(invitations);
        },
        addChannel: state.addChannel,
        removeChannel: state.removeChannel,
        addMessages: state.addMessages,
        addInvitation: state.addInvitation,
        removeInvitation: state.removeInvitation,
        clear: state.clear,
    };

}

