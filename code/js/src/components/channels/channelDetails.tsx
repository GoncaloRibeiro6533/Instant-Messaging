import * as React from 'react';
import { useParams } from 'react-router-dom';
import { ChannelRepo } from '../../service/mock/repo/ChannelRepo';
import { ChannelMember } from '../../domain/ChannelMember';

export function ChannelDetails() {
    const { channelId } = useParams();
    const [channelName, setChannelName] = React.useState('');
    const [channelMembers, setChannelMembers] = React.useState<ChannelMember[]>([]);
    const [error, setError] = React.useState('');

    React.useEffect(() => {
        console.log('Channel ID:', channelId);
        const repo = new ChannelRepo();
        const channel = repo.getChannelById(Number(channelId));
        console.log('Channel:', channel);
        if (channel) {
            setChannelName(channel.name);
            const members = repo.getChannelMembers(channel.creator, Number(channelId));
            setChannelMembers(members);
        } else {
            setError('Channel not found');
        }
    }, [channelId]);

    if (error) {
        return <div style={{ textAlign: 'center', marginTop: '50px' }}>{error}</div>;
    }

    return (
        <div style={{ textAlign: 'center', marginTop: '50px' }}>
            <h1>{channelName}</h1>
            <h2>Channel Members</h2>
            <ul>
                {channelMembers.map((member) => (
                    <li key={member.user.id}>{member.user.username}</li>
                ))}
            </ul>
        </div>
    );
}