import * as React from 'react';
import List from '@mui/material/List';
import ListItemText from '@mui/material/ListItemText';
import Divider from '@mui/material/Divider';
import Avatar from '@mui/material/Avatar';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import SearchIcon from '@mui/icons-material/Search';
import { Channel } from '../../../domain/Channel';
import { AuthContext } from '../../auth/AuthProvider';
import { useChannelList } from './useChannelList';
import { services } from '../../../App';
import Logo from "../../../../public/logo.png";
import ListItemButton from "@mui/material/ListItemButton";
import { getRandomColor } from '../../utils/channelLogoColor';
import { Channel as ChannelComponent } from '../channel';

export function ChannelsList() {
    const { user } = React.useContext(AuthContext);
    const [state, loadChannels] = useChannelList();
    const [searchChannels, setSearchChannels] = React.useState('');
    const [searchResults, setSearchResults] = React.useState<Channel[]>([]);
    const [selectedChannel, setSelectedChannel] = React.useState<Channel | null>(null);

    React.useEffect(() => {
        loadChannels();
    }, [user.user.id]);

    const handleSearch = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const term = event.target.value;
        setSearchChannels(term);
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

    const channelsToDisplay = searchChannels ? searchResults : state.channels;

    return (
        <div style={{ display: 'flex', height: '100vh' }}>
            <div style={{ width: '250px', borderRight: '1px solid #ddd', paddingTop: '16px' }}>
                <TextField
                    label="Search Channels"
                    variant="outlined"
                    fullWidth
                    value={searchChannels}
                    onChange={handleSearch}
                    style={{ marginBottom: '16px' }}
                    InputProps={{ //todo fix this deprecated?
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
                            <ListItemButton onClick={() => setSelectedChannel(channel)}>
                                <Avatar sx={{ bgcolor: getRandomColor(channel.id) }}>
                                    {channel.name.charAt(0)}
                                </Avatar>
                                <ListItemText primary={channel.name} sx={{ marginLeft: 2 }} />
                            </ListItemButton>
                            <Divider />
                        </React.Fragment>
                    ))}
                </List>
            </div>
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                {selectedChannel && <ChannelComponent channel={selectedChannel} />}
                <div style={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    {!selectedChannel && (
                        <img src={Logo} alt="Logo" width={250} style={{ opacity: 0.5, maxWidth: '100%', maxHeight: '100%', objectFit: 'contain' }}/>
                    )}
                </div>
            </div>
        </div>
    );
}