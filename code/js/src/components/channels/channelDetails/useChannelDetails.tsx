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


export function useChannelDetails() : [
    State,
    (channelId: string) => void
]{
    const [state, dispatch] = React.useReducer(reduce, { name: 'idle' });
    const [auth] = useAuth();
    const { channels, channelMembers, setChannelMembers, addChannels } = useData();
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
            const loadedChannel = channels.get(parsedId);
            const loadedMembers = channelMembers.get(parsedId);

            if (loadedChannel && loadedMembers) {
                if (selectedChannelIdRef.current === parsedId) {
                    dispatch({ type: 'success', channel: loadedChannel, members:loadedMembers, previouslyLoaded: true });
                }
                return;
            }
            dispatch({ type: 'load', channelId: parsedId });
            const loadedChannels = loadedChannel === undefined &&  await services.channelService.getChannelsOfUser(auth.id) || loadedChannel
            const members = loadedMembers === undefined && await services.channelService.getChannelMembers(parsedId) || loadedMembers
            if(loadedChannel === undefined && loadedChannels instanceof Map)  addChannels(loadedChannels)
            if(loadedMembers === undefined) setChannelMembers(new Map([[parsedId, members]]))
            if (selectedChannelIdRef.current === parsedId) {
                const channel = !(loadedChannels instanceof Channel) && Array.from(loadedChannels.keys()).find((channel: Channel) => channel.id === parsedId)
                || loadedChannel
                dispatch({ type: 'success', channel:channel , members, previouslyLoaded: false });
            }
        } catch (error: any) {
            const errorMessage = error.message || 'An error occurred';
            dispatch({ type: 'error', error: errorMessage });
        }
    }
    return [state, loadChannel]
}