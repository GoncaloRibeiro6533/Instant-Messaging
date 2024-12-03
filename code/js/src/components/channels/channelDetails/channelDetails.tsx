import * as React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {Box, Button, Typography, TextField, Avatar, Snackbar, Chip} from '@mui/material';
import ExitToAppIcon from '@mui/icons-material/ExitToApp';
import { Edit } from "@mui/icons-material";
import { AuthContext } from '../../auth/AuthProvider';
import { Channel } from '../../../domain/Channel';
import { ChannelMember } from '../../../domain/ChannelMember';
import { ChannelRepo } from '../../../service/mock/repo/ChannelRepo';
import { useLeaveChannel } from './useLeaveChannel';
import { useEditChannelName } from './useEditChannelName';
import { getRandomColor } from '../../utils/channelLogoColor';
import Grid from '@mui/material/Grid';
import {Role} from "../../../domain/Role";

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
    const location = useLocation();
    const [leaveState, leaveChannel] = useLeaveChannel(user.token);
    const [invitationMessage, setInvitationMessage] = React.useState<string | null>(null);
    const [openSnackbar, setOpenSnackbar] = React.useState(false);

    const {
        newChannelName,
        setNewChannelName,
        isEditing,
        handleEditClick,
        handleSaveClick
    } = useEditChannelName(channel.id, channel.name, loadChannels);

    React.useEffect(() => {
        const fetchMembers = async () => {
            try {
                //TODO
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
        fetchMembers();
    }, [channel, user.user]);

    React.useEffect(() => {
        setNewChannelName(channel.name);
    }, [channel.name, setNewChannelName]);

    React.useEffect(() => {
        if (location.state?.invitedUser) {
            setInvitationMessage(`${location.state.invitedUser} invited to channel!`);
            setOpenSnackbar(true);
            setTimeout(() => setOpenSnackbar(false), 3000);
        } else if (location.state?.invitedEmail) {
            setInvitationMessage(`Invitation sent to: ${location.state.invitedEmail}`);
            setOpenSnackbar(true);
            setTimeout(() => setOpenSnackbar(false), 3000);
        }
    }, [location.state]);

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
        return <Typography variant="h6" color="error" align="center" mt={5}>{error}</Typography>;
    }

    return (
        <Box textAlign="center" mt={5}>
            <Avatar sx={{ bgcolor: getRandomColor(channel.id), width: 100, height: 100, margin: '0 auto', fontSize: 50 }}>
                {newChannelName.charAt(0)}
            </Avatar>
            {isEditing ? (
                <Box mt={2}>
                    <TextField
                        value={newChannelName}
                        onChange={(e) => setNewChannelName(e.target.value)}
                        variant="outlined"
                        size="small"
                    />
                    <Button variant="contained" color="primary" onClick={handleSaveClick} sx={{ ml: 2, textTransform: 'none' }}>
                        Save
                    </Button>
                </Box>
            ) : (
                <Typography variant="h4" mt={2}>
                    {newChannelName}
                    <Button variant="text" color="primary" onClick={handleEditClick} sx={{ ml: 2, textTransform: 'none' }}>
                        <Edit sx={{ mr: 1 }} />
                        Edit
                    </Button>
                </Typography>
            )}
            <Typography variant="body2" mt={1}>Number of members: {channelMembers.length}</Typography>
            <Typography variant="h6" mt={2}>Channel Members</Typography>
            {channelMembers.length > 0 ? (
                <Grid container direction="column" alignItems="center" spacing={2} mt={2}>
                    {channelMembers.map((member) => (
                        <Grid item key={member.user.id} xs={12} container alignItems="center" justifyContent="center">
                            <Grid item xs={4}>
                                <Box p={1} border={1} borderRadius={2} display="flex" alignItems="center" justifyContent="space-between" bgcolor="#f0f0f0">
                                    <Typography variant="body1">{member.user.username}</Typography>
                                    <Chip
                                        label={member.role}
                                        size="small"
                                        sx={{
                                            backgroundColor: member.role === Role.READ_WRITE ? 'green' : '#2f2f2f',
                                            color: 'white'
                                        }}
                                    />
                                </Box>
                            </Grid>
                        </Grid>
                    ))}
                </Grid>
            ) : (
                <Typography variant="body1" mt={2}>No members found in this channel.</Typography>
            )}
            <Button
                variant="contained"
                color="error"
                onClick={handleLeaveChannelClick}
                disabled={leaveState.name === 'leaving'}
                sx={{ mt: 3, textTransform: 'none' }}
            >
                <ExitToAppIcon sx={{ mr: 1 }} />
                {leaveState.name === 'leaving' ? 'Leaving...' : 'Leave Channel'}
            </Button>
            <Button
                variant="contained"
                color="primary"
                onClick={() => navigate('/invitation', { state: { channel, token: user.token } })}
                style={{ display: 'block', margin: '10px auto' }}
            >
                Send Invitation
            </Button>
            <Snackbar
                open={openSnackbar}
                message={invitationMessage}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                autoHideDuration={5000}
                onClose={() => setOpenSnackbar(false)}
            />
        </Box>
    );
}