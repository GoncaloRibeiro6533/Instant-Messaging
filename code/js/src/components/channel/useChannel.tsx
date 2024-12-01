import * as React from 'react';
import { Channel } from '../../domain/Channel';
import { services } from '../../App';
import { useAuth } from '../auth/AuthProvider';

type State =
    | { name: 'idle' }
    | { name: 'loading', channelId: number }
    | { name: 'error'; message: string }
    | { name: 'loaded'; channel: Channel };

type Action =
    | { type: 'load'; channelId: number }
    | { type: 'success'; channel: Channel }
    | { type: 'error'; message: string };

function reduce(state: State, action: Action): State {
    switch (state.name) {
        case 'idle':
        case 'error':
            if (action.type === 'load') {
                return { name: 'loading', channelId: action.channelId };
            }
            return state;
        case 'loading':
            if (action.type === 'load') {
                return { name: 'loading', channelId: action.channelId };
            } else if (action.type === 'success' && action.channel.id === state.channelId) {
                return { name: 'loaded', channel: action.channel };
            } else if (action.type === 'error') {
                return { name: 'error', message: action.message };
            }
            return state;
        case 'loaded':
            if (action.type === 'load') {
                return { name: 'loading', channelId: action.channelId };
            }
            return state;
        default:
            return state;
    }
}

export function useChannel(): [
    State,
    (channelId: string) => void 
] {
    const [state, dispatch] = React.useReducer(reduce, { name: 'idle' });
    const [auth] = useAuth();

     async function loadChannel(channelId: string) {
        const parsedId = parseInt(channelId);
        if (isNaN(parsedId)) {
            dispatch({ type: 'error', message: 'Invalid channel ID' });
            return;
        }

        dispatch({ type: 'load', channelId: parsedId });

        try {
            const channel = await services.channelService.getChannelById(auth.token, parsedId);
            dispatch({ type: 'success', channel });
        } catch (error: any) {
            const errorMessage = error.message || 'An error occurred';
            dispatch({ type: 'error', message: errorMessage });
        }
    }

    return [state, loadChannel];
}
