import * as React from 'react';
import { Channel } from "../../../domain/Channel";
import { AuthContext } from "../../auth/AuthProvider";
import { ChannelRepo } from "../../../service/mock/repo/ChannelRepo";
import { ChannelMember } from "../../../domain/ChannelMember";
import { useLeaveChannel } from "./useLeaveChannel";
import { useNavigate } from 'react-router-dom';

interface ChannelDetailsProps {
    channel: Channel;
    onLeave: () => void;
    loadChannels: () => void;
    handleLeaveChannel: (channelId: number) => void;
}

export function ChannelDetails({ channel, onLeave, loadChannels }: ChannelDetailsProps) {
    const { user } = React.useContext(AuthContext);
    const [channelMembers, setChannelMembers] = React.useState<ChannelMember[]>([]);
    const [error, setError] = React.useState('');
    const navigate = useNavigate();
    const [leaveState, leaveChannel] = useLeaveChannel(user.token);

    React.useEffect(() => {
        const fetchMembers = async () => {
            try {
                const repo = new ChannelRepo();
                const members = repo.getChannelMembers(user.user, channel.id);
                if (!members) {
                    setError('No members found in this channel.');
                    return;
                }
                setChannelMembers(members);
            } catch (err) {
                setError('Failed to load channel members.');
                console.error(err);
            }
        };
        fetchMembers().then(r => r);
    }, [channel, user.user]);

    const handleLeaveChannelClick = async () => {
        try {
            await leaveChannel(channel.id);
            onLeave();
            loadChannels();
        } catch (err) {
            setError('Failed to leave channel.');
            console.error(err);
        }
    };

    React.useEffect(() => {
        if (leaveState.name === 'success') {
            onLeave();
            loadChannels();
            navigate('/channels');
        }
    }, [leaveState, onLeave, loadChannels, navigate]);

    if (error) {
        return <div style={{ textAlign: 'center', marginTop: '50px' }}>{error}</div>;
    }

    return (
        <div style={{ textAlign: 'center', marginTop: '50px' }}>
            <h1>{channel.name}</h1>
            <h2>Channel Members</h2>
            {channelMembers.length > 0 ? (
                <ul>
                    {channelMembers.map((member) => (
                        <li key={member.user.id}>{member.user.username}</li>
                    ))}
                </ul>
            ) : (
                <p>No members found in this channel.</p>
            )}
            <button
                onClick={
                handleLeaveChannelClick
            }
                disabled={leaveState.name === 'leaving'}
                style={{
                    backgroundColor: '#007BFF',
                    color: '#FFF',
                    border: 'none',
                    padding: '10px 20px',
                    borderRadius: '5px',
                    cursor: leaveState.name === 'leaving' ? 'not-allowed' : 'pointer',
                }}
            >
                {leaveState.name === 'leaving' ? 'Leaving...' : 'Leave Channel'}
            </button>
        </div>
    );
}
