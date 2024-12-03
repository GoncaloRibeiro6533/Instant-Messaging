import * as React from 'react';
import { Channel } from '../../domain/Channel';
import { services } from '../../App';
import { useAuth } from '../auth/AuthProvider';
import { useData } from '../data/DataProvider';
import { useNavigate } from 'react-router-dom';

type State =
    | { name: 'idle' }
    | { name: 'loading', channelId: number }
    | { name: 'error'; message: string }
    | { name: 'loaded'; channel: Channel };

type Action =
    | { type: 'load'; channelId: number }
    | { type: 'success'; channel: Channel, previouslyLoaded: boolean }
    | { type: 'error'; message: string };

function reduce(state: State, action: Action): State {
    switch (state.name) {
        case 'idle':
            if (action.type === 'success') {
                return { name: 'loaded', channel: action.channel };
            }
            if (action.type === 'load') {
                return { name: 'loading', channelId: action.channelId };
            }
            return state;
        case 'error':
            if (action.type === 'load') {
                return { name: 'loading', channelId: action.channelId };
            }
            if (action.type === 'success') {
                return { name: 'loaded', channel: action.channel };
            }
            return state;
        case 'loading':
            if (action.type === 'success') {
                if (action.previouslyLoaded) {
                    return { name: 'loaded', channel: action.channel };
                } else if (state.channelId === action.channel.id) {
                    return { name: 'loaded', channel: action.channel };
                } else return state
            }
            if (action.type === 'error') {
                return { name: 'error', message: action.message };
            }
            if (action.type === 'load') {
                return { name: 'loading', channelId: action.channelId };
            }
            return state;
        case 'loaded':
            if (action.type === 'load') {
                return { name: 'loading', channelId: action.channelId };
            }
            if (action.type === 'success') {
                return { name: 'loaded', channel: action.channel };
            }
            return state;
        default:
            return state;
    }
}

export function useChannel(): [
    State,
    (channelId: String) => void 
] {
    const [state, dispatch] = React.useReducer(reduce, { name: 'idle' });
    const [auth] = useAuth();
    const { addMessages, messages, channels } = useData();
    async function loadChannel(channelId: string) {
        const parsedId = parseInt(channelId);
        if (state.name === 'loading' && state.channelId === parsedId) {
            return;
        }
        if (isNaN(parsedId)) {
            dispatch({ type: 'error', message: 'Invalid channel ID' });
            return;
        }
        const channel = Array.from(channels.keys()).find(channel => channel.id === parsedId);
        if (!channel) {
            dispatch({ type: 'error', message: 'Channel not found' });
            return;
        }        
        if(messages.get(channel.id) !== undefined){
            dispatch({ type: 'success', channel: channel, previouslyLoaded: true });
            return
        }
        dispatch({ type: 'load', channelId: parsedId });
        try {
            const messages = await services.messageService.getMessages(auth.token, parseInt(channelId), 100, 0);
            addMessages(channel, messages);
            dispatch({ type: 'success', channel: channel, previouslyLoaded: false });
        } catch (error: any) {
            const errorMessage = error.message || 'An error occurred';
            dispatch({ type: 'error', message: errorMessage });
        }
    }

    return [state, loadChannel];
}
