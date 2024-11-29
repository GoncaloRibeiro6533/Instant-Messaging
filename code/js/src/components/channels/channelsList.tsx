import * as React from 'react';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Divider from '@mui/material/Divider';
import Avatar from '@mui/material/Avatar';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import SearchIcon from '@mui/icons-material/Search';
import { Channel } from '../../domain/Channel';
import { AuthContext } from '../auth/AuthProvider';
import { useChannelList } from './useChannelList';
import { services } from '../../App';

const channelColors = new Map<number, string>();

function getRandomColor(channelId: number): string {
    if (!channelColors.has(channelId)) {
        const letters = '0123456789ABCDEF';
        let color = '#';
        for (let i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        channelColors.set(channelId, color);
    }
    return channelColors.get(channelId)!;
}

export function ChannelsList() {
    const { user } = React.useContext(AuthContext);
    const [state, loadChannels] = useChannelList();
    const [searchTerm, setSearchTerm] = React.useState('');
    const [searchResults, setSearchResults] = React.useState<Channel[]>([]);

    React.useEffect(() => {
        loadChannels();
    }, [user.user.id]);

    const handleSearch = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const term = event.target.value;
        setSearchTerm(term);
        if (term) {
            try {
                const results = await services.channelService.searchChannelByName(user.token, term, 10, 0);
                setSearchResults(results);
            } catch (e) {
                console.error(e.message);
            }
        } else {
            setSearchResults([]);
        }
    };

    if (state.name === 'loading') {
        return <div>Loading...</div>;
    }

    if (state.name === 'error') {
        return <div>Error: {state.message}</div>;
    }

    const channelsToDisplay = searchTerm ? searchResults : state.channels;

    return (
        <div style={{ width: '250px', borderRight: '1px solid #ddd', height: '100vh', paddingTop: '16px' }}>
            <TextField
                label="Search Channels"
                variant="outlined"
                fullWidth
                value={searchTerm}
                onChange={handleSearch}
                style={{ marginBottom: '16px' }}
                InputProps={{
                    startAdornment: (
                        <InputAdornment position="start">
                            <SearchIcon />
                        </InputAdornment>
                    ),
                }}
            />
            <List>
                {channelsToDisplay.map((channel: Channel) => (
                    <React.Fragment key={channel.id}>
                        <ListItem>
                            <Avatar sx={{ bgcolor: getRandomColor(channel.id) }}>
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