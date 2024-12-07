import { useReducer } from 'react';
import { services } from '../../../App';
import { useAuth } from '../../auth/AuthProvider';
import { useData } from '../../data/DataProvider';
import { Channel } from '../../../domain/Channel';
import { useNavigate } from 'react-router-dom';

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
    switch (state.name) {
        case 'idle': {
            if (action.type === 'leave') {
                return { name: 'leaving' };
            }
            return state;
        }
        case 'leaving': {
            if (action.type === 'success') {
                return { name: 'success' };
            }
            if (action.type === 'error') {
                return { name: 'error', message: action.message };
            }
            return state;
        }
        case 'success': return state;
        case 'error': {
            if (action.type === 'leave') {
                return { name: 'leaving' };
            }
            return state;
        }
        default: return state;
    }
}

export function useLeaveChannel():[
    LeaveChannelState,
    (channel: Channel) => void
] {
    const [state, dispatch] = useReducer(leaveChannelReducer, { name: 'idle' });
    const  [user] = useAuth()
    const { removeChannel, removeChannelMember } = useData()
    const navigate = useNavigate()
    async function leaveChannel(channel: Channel)  {
        dispatch({ type: 'leave' });
        try {
            await services.channelService.leaveChannel(channel.id)
            removeChannel(channel)
            removeChannelMember(channel.id, user)
            navigate('/channels')
            dispatch({ type: 'success' });
        } catch (error) {
            dispatch({ type: 'error', message: error.message });
        }
    }
    return [state, leaveChannel]
}