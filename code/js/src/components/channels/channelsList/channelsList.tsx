import * as React from 'react';
import {Channel} from '../../../domain/Channel';
import {AuthContext} from '../../auth/AuthProvider';
import {useChannelList} from './useChannelList';
import {services} from '../../../App';
import Logo from "../../../../public/logo.png";
import {getRandomColor} from '../../utils/channelLogoColor';
import {useNavigate} from "react-router-dom";
import ListItemIcon from "@mui/material/ListItemIcon";
import {Add} from "@mui/icons-material";
import {Avatar, Box, Chip, Divider, InputAdornment, List, ListItemButton, ListItemText, TextField, ListItem} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import {Visibility} from "../../../domain/Visibility";
import {useLocation} from "react-router-dom";
import Typography from '@mui/material/Typography';
import {Outlet} from 'react-router-dom';
import {Role} from '../../../domain/Role';



export function ChannelsList() {
    const { user } = React.useContext(AuthContext);
    const [state, loadChannels] = useChannelList();
    const [searchChannels, setSearchChannels] = React.useState('');
    const [searchResults, setSearchResults] = React.useState<Channel[]>([]);
    const [selectedChannel, setSelectedChannel] = React.useState<Channel | null>(null);
    const navigate = useNavigate();
    const location = useLocation();

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

    const handleLeaveChannel = async (channelId: number) => {
        try {
            await services.channelService.leaveChannel(user.token, channelId);

            loadChannels();

            if (selectedChannel && selectedChannel.id === channelId) {
                setSelectedChannel(null);
            }

            console.log(`Channel ${channelId} deleted`);
        } catch (error) {
            console.error("Error deleting channel:", error.message);
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
        <Box sx={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
            <Box sx={{ width: '320px', borderRight: '1px solid #ddd', padding: '10px' }}>
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
                    InputProps={{
                        startAdornment: (
                            <InputAdornment position="start">
                                <SearchIcon />
                            </InputAdornment>
                        ),
                    }}
                />
                {/* Torne a lista rolável com overflowY: 'auto' */}
                <Box sx={{ maxHeight: 'calc(100vh - 200px)', overflowY: 'auto' }}>
                    <List>
                    {[...channelsToDisplay.entries()].map(([channel, role]: [Channel, Role]) => (
                            <React.Fragment key={channel.id}>
                                <ListItemButton onClick={() => navigate("/channels/channel/" + String(channel.id))}>
                                    <Avatar sx={{ bgcolor: getRandomColor(channel.id) }}>
                                        {channel.name.charAt(0)}
                                    </Avatar>
                                    <ListItem sx={{ marginTop: 1 }}>
                                        <Typography
                                            variant="body1"
                                            component="div"
                                            sx={{
                                                marginRight: 2,
                                                whiteSpace: 'nowrap',
                                                overflow: 'hidden',
                                                textOverflow: 'ellipsis',
                                                maxWidth: '120px',
                                                minWidth: '120px',
                                            }}
                                        >
                                            {channel.name}
                                        </Typography>
                                        <Chip
                                            label={channel.visibility}
                                            size="small"
                                            sx={{
                                                marginTop: 1,
                                                backgroundColor:
                                                    channel.visibility === Visibility.PUBLIC ? '#32B7A3' : '#E8556D',
                                            }}
                                        />
                                </ListItem>
                                </ListItemButton>
                                <Divider />
                            </React.Fragment>
                        ))} 
                    </List>
                </Box>
            </Box>
            {location.pathname === '/channels' && (
                <Box sx={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <img
                        src={Logo}
                        alt="Logo"
                        width={250}
                        style={{
                            opacity: 0.5,
                            maxWidth: '100%',
                            maxHeight: '100%',
                            objectFit: 'contain',
                        }}
                    />
                </Box>
            )}
            <Outlet />
        </Box>
    );

}