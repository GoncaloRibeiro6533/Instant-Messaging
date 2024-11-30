import * as React from 'react';
import {Channel} from '../../../domain/Channel';
import {AuthContext} from '../../auth/AuthProvider';
import {useChannelList} from './useChannelList';
import {services} from '../../../App';
import Logo from "../../../../public/logo.png";
import {getRandomColor} from '../../utils/channelLogoColor';
import {Channel as ChannelComponent} from '../channel';
import {useNavigate} from "react-router-dom";
import ListItemIcon from "@mui/material/ListItemIcon";
import {Add} from "@mui/icons-material";
import {Avatar, Box, Chip, Divider, InputAdornment, List, ListItemButton, ListItemText, TextField} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import {Visibility} from "../../../domain/Visibility";

export function ChannelsList() {
    const { user } = React.useContext(AuthContext);
    const [state, loadChannels] = useChannelList();
    const [searchChannels, setSearchChannels] = React.useState('');
    const [searchResults, setSearchResults] = React.useState<Channel[]>([]);
    const [selectedChannel, setSelectedChannel] = React.useState<Channel | null>(null);
    const navigate = useNavigate();

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
        <Box sx={{ display: 'flex', height: '100vh' }}>
            <Box sx={{ width: '250px', borderRight: '1px solid #ddd', padding: '10px' }}>
                <ListItemButton onClick={() => navigate('/createChannel')} sx={{ marginBottom: '16px' }}>
                    <ListItemIcon>
                        <Add />
                    </ListItemIcon>
                    <ListItemText primary="Create Channel" />
                </ListItemButton>
                <TextField
                    label="Search Channels"
                    variant="outlined"
                    fullWidth
                    value={searchChannels}
                    onChange={handleSearch}
                    sx={{ marginBottom: '16px' }}
                    InputProps={{ //todo try another way because its deprecated
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
                                <ListItemText
                                    primary={channel.name}
                                    secondary={
                                        <Chip
                                            label={channel.visibility}
                                            size="small"
                                            color={channel.visibility === Visibility.PUBLIC ? 'primary' : 'secondary'}
                                            sx={{ marginTop: 1 }}
                                        />
                                    }
                                    sx={{ marginLeft: 2 }}
                                />
                            </ListItemButton>
                            <Divider />
                        </React.Fragment>
                    ))}
                </List>
            </Box>
            <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                {selectedChannel && <ChannelComponent channel={selectedChannel} />}
                <Box sx={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    {!selectedChannel && (
                        <img src={Logo} alt="Logo" width={250} style={{ opacity: 0.5, maxWidth: '100%', maxHeight: '100%', objectFit: 'contain' }} />
                    )}
                </Box>
            </Box>
        </Box>
    );
}