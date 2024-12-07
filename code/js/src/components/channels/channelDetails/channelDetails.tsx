import * as React from 'react'
import {useNavigate, useLocation, useParams} from 'react-router-dom'
import { Box, Button, Typography, TextField, Avatar, Snackbar, Chip, Grid, Alert } from '@mui/material'
import ExitToAppIcon from '@mui/icons-material/ExitToApp'
import { Edit } from "@mui/icons-material"
import { AuthContext, useAuth } from '../../auth/AuthProvider'
import { Channel } from '../../../domain/Channel'
import { ChannelMember } from '../../../domain/ChannelMember'
import { ChannelRepo } from '../../../service/mock/repo/ChannelRepo'
import { useLeaveChannel } from './useLeaveChannel'
import { useEditChannelName } from './useEditChannelName'
import { getRandomColor } from '../../utils/channelLogoColor'
import { Role } from "../../../domain/Role"
import {useData} from "../../data/DataProvider"
import {services} from "../../../App"
import { useChannnelDetails } from './useChannelDetails'
import { CircularProgress } from '@mui/material'

export function ChannelDetails() {
    const [user ] = useAuth()
    const navigate = useNavigate()
    const location = useLocation()
    const { channelId } = useParams()
    const [state, loadChannel] = useChannnelDetails()
    const [editState, {onSubmit, onEdit, onChange, onCancel}] = useEditChannelName()
    const { channels, channelMembers } = useData()
    const [leaveState, leaveChannel] = useLeaveChannel()
    //const [invitationMessage, setInvitationMessage] = React.useState<string | null>(null)
    const [openSnackbar, setOpenSnackbar] = React.useState(false)

    React.useEffect(() => {
        loadChannel(channelId)
    }, [channels, channelId, channelMembers])

    /*React.useEffect(() => {
        setNewChannelName(channel.name)
    }, [channel.name, setNewChannelName])

    React.useEffect(() => {
        if (location.state?.invitedUser) {
            setInvitationMessage(`${location.state.invitedUser} invited to channel!`)
            setOpenSnackbar(true)
            setTimeout(() => setOpenSnackbar(false), 3000)
        } else if (location.state?.invitedEmail) {
            setInvitationMessage(`Invitation sent to: ${location.state.invitedEmail}`)
            setOpenSnackbar(true)
            setTimeout(() => setOpenSnackbar(false), 3000)
        }
    }, [location.state])

    const handleLeaveChannelClick = async () => {
        try {
            await leaveChannel(channel.id)
            onLeave()
            loadChannels()
        } catch (err) {
            setError('Failed to leave channel.')
            console.error(err)
        }
    }

    React.useEffect(() => {
        if (leaveState.name === 'success') {
            onLeave()
            loadChannels()
            navigate('/channels')
        }
    }, [leaveState, onLeave, loadChannels, navigate])

    if (error) {
        return <Typography variant="h6" color="error" align="center" mt={5}>{error}</Typography>
    }*/
    return (
        <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        {(state.name === 'error' || leaveState.name === 'error' || editState.name === 'error' || editState.name === 'editing' && editState.error !== undefined) &&(
            <Alert severity="error" sx={{ 
                mb: 2,
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                zIndex: 30,
                }}>
                {state.name === 'error' && state.error}
                {leaveState.name === 'error' && leaveState.message}
                {editState.name === 'error' && editState.error}
                {editState.name === 'editing' && editState.error}
            </Alert>
        )}    
        { state.name === 'loading' || leaveState.name === 'leaving' && (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexGrow: 1 }}>
                <CircularProgress size="60px" />
                {leaveState.name === 'leaving' && <Typography variant="h6" mt={2}>Leaving channel...</Typography> }
            </Box>)}
        {state.name === 'displaying' &&  leaveState.name === 'idle' &&
            (
            <Box textAlign="center" mt={5}>
                <Avatar sx={{ bgcolor: getRandomColor(state.channel.id), width: 100, height: 100, margin: '0 auto', fontSize: 50 }}>
                {state.channel.name.charAt(0)}
                </Avatar>
            </Box>)}
            {state.name === 'displaying' && (editState.name === 'displaying' || editState.name === 'editing') && (
                <Box
                    sx={{
                    position: 'fixed', 
                    top: 0,
                    left: 0,
                    width: '100vw',
                    height: '100vh',
                    backgroundColor: 'rgba(0, 0, 0, 0.5)', 
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    zIndex: 10,
                    }}
                >
                    <Box
                    sx={{
                        backgroundColor: '#ffff', 
                        padding: 4,
                        borderRadius: 4,
                        boxShadow: '0px 4px 20px rgba(0, 0, 0, 0.1)', 
                        width: '90%',
                        maxWidth: '500px', 
                        textAlign: 'center',
                    }}
                    >
                    <form
                        onSubmit={(ev) => {
                        ev.preventDefault();
                        onSubmit(ev);
                        }}
                    >
                        <TextField
                        label="Edit channel name"
                        variant="outlined"
                        fullWidth
                        value={editState.newChannelName}
                        onChange={(ev) => onChange(state.channel, ev)}
                        sx={{ marginBottom: 2 }}
                        />
                        <Box
                        sx={{
                            display: 'block',
                            justifyContent: 'space-between',
                        }}
                        >
                        <Button
                            type="submit"
                            variant="contained"
                            disabled={editState.newChannelName === state.channel.name || !editState.newChannelName.trim()}
                            sx={{
                            backgroundColor: '#28a745',
                            color: '#fff',
                            borderRadius: 20,
                            textTransform: 'none',
                            marginRight: 1,
                            '&:hover': {
                                backgroundColor: '#218838',
                            },
                            }}
                        >
                            Save
                        </Button>
                        <Button
                            onClick={onCancel}
                            variant="outlined"
                            sx={{
                            borderColor: '#dc3545',
                            color: '#dc3545',
                            borderRadius: 20,
                            textTransform: 'none',
                            marginLeft: 1,
                            '&:hover': {
                                backgroundColor: '#f8d7da',
                                borderColor: '#dc3545',
                            },
                            }}
                        >
                            Cancel
                        </Button>
                        </Box>
                    </form>
                    </Box>
                </Box>
                )}

            {editState.name === 'idle'&& state.name == 'displaying' && leaveState.name === 'idle' && (
                <Box textAlign="center" mt={5}>
                    <Typography variant="h4" mt={2}>
                        {state.channel.name}
                        {state.channel.creator.id === user.user.id && (
                        <Button variant="text" color="primary" onClick={() => onEdit(state.channel)} sx={{ ml: 2, textTransform: 'none' }}>
                            <Edit sx={{ mr: 1 }} />
                            Edit
                        </Button>)}
                    </Typography>
                </Box>    
            )}
            {state.name === 'displaying' && leaveState.name === 'idle' && (
                <Box textAlign="center" mt={2}>
                    <Typography variant="body2" mt={1}>Number of members: {state.members.length}</Typography>
                    <Typography variant="h6" mt={2}>Channel Members</Typography>
                    <Grid container direction="column" alignItems="center" spacing={2} mt={2}>
                            {state.members.map((member) => (
                                <Grid item key={member.user.id} xs={12} container alignItems="center" justifyContent="center">
                                <Grid item xs={4}>
                                    <Box p={1} border={1} borderRadius={2} display="flex" alignItems="center" justifyContent="space-between" bgcolor="#f0f0f0">
                                        <Typography variant="body1">{member.user.username}</Typography>
                                        <Box display="flex" alignItems="center" justifyContent="space-between" gap={1}>
                                            {member.user.id === state.channel.creator.id && (
                                                <Chip
                                                    label="Creator"
                                                    size="small"
                                                    sx={{
                                                        backgroundColor: 'blue',
                                                        color: 'white'
                                                    }}
                                                />
                                            )}
                                            <Chip
                                                label={member.role}
                                                size="small"
                                                sx={{
                                                    backgroundColor: member.role === Role.READ_WRITE ? 'green' : '#2f2f2f',
                                                    color: 'white'
                                                }}
                                            />
                                        </Box>
                                    </Box>
                                </Grid>
                            </Grid>
                        ))}
                    </Grid>
                    <Button
                        variant="contained"
                        color="error"
                        onClick={() => leaveChannel(state.channel)}
                        sx={{ mt: 3, textTransform: 'none' }}
                    >
                        Leave Channel  
                        <ExitToAppIcon sx={{ mr: 1 }} />
                    </Button>
                </Box>
            )}
           
        </Box>
    )
}


 {/*
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
        </Box>*/}