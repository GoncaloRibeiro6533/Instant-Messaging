import { useReducer, useCallback } from 'react';
import { services } from '../../../App';

type LeaveChannelState =
    | { name: 'idle' }
    | { name: 'leaving' }
    | { name: 'success' }
    | { name: 'error', message: string };

type LeaveChannelAction =
    | { type: 'leave' }
    | { type: 'success' }
    | { type: 'error', message: string };

function leaveChannelReducer(state: LeaveChannelState, action: LeaveChannelAction): LeaveChannelState {
    switch (action.type) {
        case 'leave':
            return { name: 'leaving' };
        case 'success':
            return { name: 'success' };
        case 'error':
            return { name: 'error', message: action.message };
        default:
            return state;
    }
}

export function useLeaveChannel(token: string) {
    const [state, dispatch] = useReducer(leaveChannelReducer, { name: 'idle' });

    const leaveChannel = useCallback(async (channelId: number) => {
        dispatch({ type: 'leave' });
        try {
            await services.channelService.leaveChannel(token, channelId);
            dispatch({ type: 'success' });
        } catch (error) {
            dispatch({ type: 'error', message: error.message });
        }
    }, []);

    return [state, leaveChannel] as const;
}