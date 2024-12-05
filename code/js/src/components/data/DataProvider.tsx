import {Channel} from "../../domain/Channel"
import {Role} from "../../domain/Role"
import {Message} from "../../domain/Message"
import {ChannelInvitation} from "../../domain/ChannelInvitation"
import * as React from "react"
import {ChannelMember} from "../../domain/ChannelMember";

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
    channelMembers: Map<Number, ChannelMember[]> // <channelId, User>
    addChannelMember: (channelId : Number, user : ChannelMember[]) => void,
    removeChannelMember: (channelId : Number, user:ChannelMember) => void,
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
    channelMembers: new Map(),
    addChannelMember: () => {},
    removeChannelMember: () => {},
    clear : () => {}
})

export function DataProvider({ children }: { children: React.ReactNode }) : React.JSX.Element {
   
    //Initial state
    const initialChannels = new Map();
    const initialMessages =  new Map();
    const initialInvitations: ChannelInvitation[] = []
    const initialChannelMembers = new Map<Number, ChannelMember[]>();
    //Hooks
    const [channels, setChannels] = React.useState<Map<Channel,Role>>(initialChannels);
    const [messages, setMessages] = React.useState<Map<Number,Message[]>>(initialMessages);
    const [invitations, setInvitations] = React.useState<Array<ChannelInvitation>>(initialInvitations);
    const [channelMembers, setChannelMembers] = React.useState<Map<Number, ChannelMember[]>>(initialChannelMembers);

    const addChannel = (channel : Channel, role : Role) => {
        setChannels((prevChannels) => {
            const newChannels = new Map(prevChannels);
            newChannels.set(channel,role);
            return newChannels;
        });
    }
    const removeChannel = (channel : Channel) => {
        setChannels((prevChannels) => {
            const newChannels = new Map(prevChannels);
            newChannels.delete(channel);
            return newChannels;
        });

        setMessages((prevMessages) => {
            const newMessages = new Map(prevMessages);
            newMessages.delete(channel.id);
            return newMessages;
        });
    }

    const updateChannel = (channel : Channel) => {
        setChannels((prevChannels) => {
            const newChannels = new Map(prevChannels);
            newChannels.set(channel,prevChannels.get(channel));
            return newChannels;
        });
    }
    const addMessages = (channel : Channel, messagesToAdd : Message[]) => {
        setMessages((prevMessages) => {
            const newMessages = new Map(prevMessages)
            const channelMessages = newMessages.get(channel.id) || []
            newMessages.set(channel.id, [...messagesToAdd, ...channelMessages])
            return newMessages;
        });
    }
    const addInvitation = (invitation : ChannelInvitation) => {
        setInvitations((prevInvitations) => {
            return [...prevInvitations, invitation];
        });
    }

    const removeInvitation = (invitation : ChannelInvitation) => {
        setInvitations((prevInvitations) => {
            const newInvitations = [...prevInvitations];
            const index = newInvitations.indexOf(invitation);
            newInvitations.splice(index,1);
            return newInvitations;
        })
    }

    const addChannelMember = (channelId : Number, user : ChannelMember[]) => {
        const newChannelMembers = new Map(channelMembers);
        const users = newChannelMembers.get(channelId) || [];
        newChannelMembers.set(channelId, [...users, ...user]);
        setChannelMembers(newChannelMembers)
    }

    const removeChannelMember = (channelId : Number, user: ChannelMember) => {
        const newChannelMembers = new Map([...channelMembers]);
        const users = newChannelMembers.get(channelId) || [];
        const index = users.indexOf(user);
        users.splice(index,1);
        newChannelMembers.set(channelId, users);
        setChannelMembers(newChannelMembers);
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
            channelMembers,
            addChannelMember,
            removeChannelMember,
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
        channelMembers: state.channelMembers,
        addChannelMember: state.addChannelMember,
        removeChannelMember: state.removeChannelMember,
        clear: state.clear,
    };

}

