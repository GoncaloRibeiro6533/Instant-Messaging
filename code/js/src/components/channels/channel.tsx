import * as React from 'react';
import { Channel } from '../../domain/Channel';
import Avatar from '@mui/material/Avatar';
import { getRandomColor } from '../utils/channelLogoColor';
import ListItemButton from "@mui/material/ListItemButton";
import { useNavigate } from 'react-router-dom';

interface ChannelProps {
    channel: Channel;
    onLeave: () => void;
}

export function Channel({ channel, onLeave }: ChannelProps) {
    const navigate = useNavigate();

    const handleClick = () => {
        navigate(`/channel/${channel.id}`);
    };

    /*const handleLeaveClick = () => {
        onLeave();
    };*/

    return (
        <div style={{ width: '100%', padding: '16px', borderBottom: '1px solid #ddd', display: 'flex', alignItems: 'center' }}>
            <ListItemButton onClick={handleClick}>
                <Avatar sx={{ bgcolor: getRandomColor(channel.id), width: 40, height: 40 }}>
                    {channel.name.charAt(0)}
                </Avatar>
                <h2 style={{ marginLeft: '16px' }}>{channel.name}</h2>
            </ListItemButton>
        </div>
    );
}