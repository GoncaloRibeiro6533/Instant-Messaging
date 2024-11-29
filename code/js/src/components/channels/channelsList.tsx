/*
import * as React from 'react';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Divider from '@mui/material/Divider';
import Avatar from '@mui/material/Avatar';
import { Channel } from '../../domain/Channel';
import { AuthContext } from '../auth/AuthProvider';
import { useChannelList } from './useChannelList';

function getRandomColor() {
    const letters = '0123456789ABCDEF';
    let color = '#';
    for (let i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

export function ChannelsList() {
    const { user } = React.useContext(AuthContext);
    const [state, loadChannels] = useChannelList();

    React.useEffect(() => {
        loadChannels();
    }, [user.user.id]);

    if (state.name === 'loading') {
        return <div>Loading...</div>;
    }

    if (state.name === 'error') {
        return <div>Error: {state.message}</div>;
    }

    return (
        <div style={{ width: '250px', borderRight: '1px solid #ddd', height: '100vh' }}>
            <List>
                {state.channels.map((channel: Channel) => (
                    <React.Fragment key={channel.id}>
                        <ListItem>
                            <Avatar sx={{ bgcolor: getRandomColor() }}>
                                {channel.name.charAt(0)}
                            </Avatar>
                            <ListItemText primary={channel.name} sx={{ marginLeft: 2 }} />
                        </ListItem>
                        <Divider />
                    </React.Fragment>
                ))}
            </List>
        </div>
    );
}

  */