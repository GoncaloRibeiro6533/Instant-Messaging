
import { useData } from "../../data/DataProvider";
import { useAuth } from "../../auth/AuthProvider";
import { Channel } from "../../../domain/Channel";
import * as React from "react"
import { ChannelMember } from "../../../domain/ChannelMember";
import { services } from "../../../App";


type State =
    | { name: 'idle'}
    | { name: "displaying", channel: Channel, members: ChannelMember[], previouslyLoaded: boolean }
    | { name: "loading", channelId: number }
    | { name: "error", error: string }

type Action =
    | { type: "success", channel: Channel, members: ChannelMember[], previouslyLoaded: boolean }
    | { type: "error", error: string }
    | { type: "load", channelId: number };

function reduce(state: State, action: Action): State {
    switch (state.name) {
        case 'idle':
            if(action.type === "load") {
                return { name: "loading", channelId: action.channelId }
            }
            if(action.type === "success") {
                return { name: "displaying", channel: action.channel, members: action.members, previouslyLoaded: action.previouslyLoaded}
            }
            return state
        case 'loading':
            if(action.type === "success") {
                return { name: "displaying", channel: action.channel, members: action.members, previouslyLoaded: action.previouslyLoaded}
            }
            if(action.type === "error") {
                return { name: "error", error: action.error}
            }
            return state    
        case 'error':
            if(action.type === "load") {
                return { name: "loading", channelId: action.channelId }
            }
            return state   
        case 'displaying':
            if(action.type === "success") {
                return { name: "displaying", channel: action.channel, members: action.members, previouslyLoaded: action.previouslyLoaded}
            }
            if(action.type === "load") {
                return { name: "loading", channelId: action.channelId }
            }
            return state
        default:
            return state
        }

}


export function useChannnelDetails() : [
    State,
    (channelId: string) => void
]{
    const [state, dispatch] = React.useReducer(reduce, { name: 'idle' });
    const [auth] = useAuth();
    const { channels, channelMembers, addChannel, addChannelMember } = useData();
    const selectedChannelIdRef = React.useRef<number | null>(null);
    async function loadChannel(channelId: string) {
        try {
            const parsedId = parseInt(channelId);
            if (state.name === 'loading' && state.channelId === parsedId) {
                return;
            }
            if (isNaN(parsedId)) {
                dispatch({ type: 'error', error: 'Invalid channel ID' });
                return;
            }
            selectedChannelIdRef.current = parsedId;

            const loadedChannel = Array.from(channels.keys()).find(channel => channel.id === parsedId) || null;
            const loadedMembers = channelMembers.get(parsedId) || null;

            if (loadedChannel && loadedMembers) {
                if (selectedChannelIdRef.current === parsedId) {
                    dispatch({ type: 'success', channel: loadedChannel, members:loadedMembers, previouslyLoaded: true });
                }
                return;
            }
            const loadedChannels = loadedChannel === null &&  await services.channelService.getChannelsOfUser(auth.token, auth.user.id);
            const channel = loadedChannel || Array.from(loadedChannels.keys()).find(channel => channel.id === parsedId) || null;
            const members =loadedMembers === null && await services.channelService.getChannelMembers(auth.token, parsedId) || loadedMembers
            dispatch({ type: 'load', channelId: parsedId });
            if(loadedChannel === null) addChannel(channel, members.find(member => member.user.id === auth.user.id).role)
            if(loadedMembers === null) addChannelMember(parsedId, members)
            if (selectedChannelIdRef.current === parsedId) {
                dispatch({ type: 'success', channel:channel , members, previouslyLoaded: false });
            }
        } catch (error: any) {
            const errorMessage = error.message || 'An error occurred';
            dispatch({ type: 'error', error: errorMessage });
        }
    }
    return [state, loadChannel]

}