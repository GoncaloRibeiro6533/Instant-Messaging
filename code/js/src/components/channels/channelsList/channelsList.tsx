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
import {Avatar, Box, Chip, CircularProgress, Divider, InputAdornment, List, ListItemButton, ListItemText, TextField, ListItem, Alert} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import {Visibility} from "../../../domain/Visibility";
import {useLocation} from "react-router-dom";
import Typography from '@mui/material/Typography';
import {Outlet} from 'react-router-dom';
import {Role} from '../../../domain/Role';
import { useData } from '../../data/DataProvider';
import { useState } from 'react';



export function ChannelsList() {
    const { user } = React.useContext(AuthContext);
    const [state, loadChannels] = useChannelList();
    const [searchChannels, setSearchChannels] = React.useState('');
    const [searchResults, setSearchResults] = React.useState<Channel[]>([]);
    const navigate = useNavigate();
    const location = useLocation();
    const { channels, addChannel, addChannelMember } = useData()

    React.useEffect(() => {
        loadChannels()
    }, [user.id, channels]);

    const handleSearch = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const term = event.target.value;
        setSearchChannels(term);
        if (term) {
            try {
                const results = await services.channelService.searchChannelByName(term, 10, 0);
                setSearchResults(results);
            } catch (e) {
                console.error(e.message);
            }
        } else {
            setSearchResults([]);
        }
    };
 

    const [open, setOpen] = useState(false);
    const [selectedChannel, setSelectedChannel] = useState<any>(null);

    async function handleJoinChannel(channel: Channel) {
        try {
            await services.channelService.joinChannel(channel.id, Role.READ_WRITE);
            addChannel(channel, Role.READ_WRITE);
            addChannelMember(channel.id, [{user: user, role: Role.READ_WRITE}])
            navigate(`/channels/channel/${channel.id}`);
        } catch (e) {
            console.error(e.message);
        }
    }

    
    const channelsToDisplay = searchChannels.trim()
        ? searchResults : (state.name == 'loaded' && Array.from(channels.keys()))
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
                 { state.name === "error" && (
                   <Alert severity="error" sx={{ marginBottom: 2 }}>
                   {state.message}
               </Alert>)
                }
                {state.name === 'loading' && 
                    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexGrow: 1 }}>
                            <CircularProgress size="40px" />
                        </Box>
                }
                { state.name === 'stopped' && ( <Typography variant="h6" sx={{ textAlign: 'center' }}>You don't have any channels</Typography>)}
                {/* Torne a lista rolável com overflowY: 'auto' */}
                {state.name === 'loaded' && (
                <Box sx={{ maxHeight: 'calc(100vh - 200px)', overflowY: 'auto' }}>
                    <List>
                        {channelsToDisplay.map((channel) => {
                            const isUserInChannel = Array.from(channels.keys()).some((c) => c.id === channel.id);
                            return (
                                <React.Fragment key={channel.id}>
                                    <ListItemButton onClick={() => {
                                        if (isUserInChannel) {
                                            navigate("/channels/channel/" + String(channel.id));
                                        } else {
                                            handleJoinChannel(channel);
                                        }
                                    }}>
                                        <Avatar sx={{ bgcolor: getRandomColor(channel.id) }}>
                                            {channel.name.charAt(0)}
                                        </Avatar>
                                        <ListItem sx={{ marginTop: 1, flexDirection: 'column', alignItems: 'flex-start' }}>
                                            <Box sx={{ display: 'flex', alignItems: 'center', width: '100%' }}>
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
                                                        backgroundColor:
                                                            channel.visibility === Visibility.PUBLIC ? '#32B7A3' : '#E8556D',
                                                    }}
                                                />
                                            </Box>
                                            {!isUserInChannel && (
                                                <Typography variant="body2" color="textSecondary" sx={{ marginTop: 1 }}>
                                                    You are not in this channel
                                                </Typography>
                                            )}
                                        </ListItem>
                                    </ListItemButton>
                                    <Divider />
                                </React.Fragment>
                            );
                        })}
                    </List>
                </Box>)}
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