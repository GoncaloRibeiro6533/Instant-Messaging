import * as React from 'react'
import {useNavigate, useLocation, useParams} from 'react-router-dom'
import { Box, Button, Typography, TextField, Avatar, Chip, Grid, Alert, List, ListItem } from '@mui/material'
import ExitToAppIcon from '@mui/icons-material/ExitToApp'
import { Edit } from "@mui/icons-material"
import {useAuth } from '../../auth/AuthProvider'
import { useLeaveChannel } from './useLeaveChannel'
import { useEditChannelName } from './useEditChannelName'
import { getRandomColor } from '../../utils/channelLogoColor'
import { Role } from "../../../domain/Role"
import { ChannelMember } from "../../../domain/ChannelMember"
import {useData} from "../../data/DataProvider"
import { useChannelDetails } from './useChannelDetails'
import { CircularProgress } from '@mui/material'
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { InvitationOptions } from '../../invitation/invitationOptions'

export function ChannelDetails() {
    const [user ] = useAuth()
    const navigate = useNavigate()
    const { channelId } = useParams()
    const [state, loadChannel] = useChannelDetails()
    const [editState, {onSubmit, onEdit, onChange, onCancel}] = useEditChannelName()
    const { channels, channelMembers } = useData()
    const [leaveState, leaveChannel] = useLeaveChannel()
    const [popUp, setPopUp] = React.useState(false)

    React.useEffect(() => {
        if(state.name === 'idle')
        loadChannel(channelId)
    }, [channels, channelMembers])

    const handleBackClick = () => {
        navigate('/channels/channel/' + channelId);
    };

    const handleSendInvitation = () => {
        if (state.name !== "error" && state.name !== "loading" && state.name !== "idle") {
            setPopUp(true)
        }
    };

    return (
        <Box sx={{ flex: 1, display: 'flex', align: 'center', flexDirection: 'column', maxHeight: '87vh', overflowY: 'auto' }}>
            
            {/* Error Handling */}
            {(state.name === 'error' || leaveState.name === 'error' || editState.name === 'error' || 
              (editState.name === 'editing' && editState.error !== undefined)) && (
                <Alert severity="error" sx={{ mb: 2, display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 30 }}>
                    {state.name === 'error' && state.error}
                    {leaveState.name === 'error' && leaveState.message}
                    {editState.name === 'error' && editState.error}
                    {editState.name === 'editing' && editState.error}
                </Alert>
            )}
    
            {/* Back Button */}
            {state.name === 'displaying' && leaveState.name === 'idle' && (
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleBackClick}
                        startIcon={<ArrowBackIcon />}
                        sx={{ textTransform: 'none', m: 2 }}
                    >
                        Back
                    </Button>
                </Box>
            )}
            {/* Loading State */}
            {state.name === "loading" && (
                <Box
                    sx={{
                        display: "flex",
                        justifyContent: "center",
                        alignItems: "center",
                        height: "100vh",
                        width: "100vw",
                        position: "fixed",
                        top: 0,
                        left: 0,
                        zIndex: 1000,
                        flexDirection: "column"
                    }}
                >
                    <CircularProgress size={60} />
                </Box>
            )}    
            {/* Channel Avatar */}
            {state.name === 'displaying' && leaveState.name === 'idle' && (
                <Box textAlign="center" mt={5} sx={{ display: 'flex', justifyContent: 'center', marginTop:0 }}>
                    <Avatar 
                        sx={{ bgcolor: getRandomColor(state.channel.id), width: 100, height: 100, m: '0 auto', fontSize: 50, marginTop: 2 }}
                    >
                        {state.channel.name.charAt(0)}
                    </Avatar>
                </Box>
            )}
    
            {/* Edit Channel Modal */}
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
                            backgroundColor: '#fff',
                            padding: 4,
                            borderRadius: 4,
                            boxShadow: '0px 4px 20px rgba(0, 0, 0, 0.1)',
                            width: '90%',
                            maxWidth: '500px',
                            textAlign: 'center',
                        }}
                    >
                        <form onSubmit={(ev) => { ev.preventDefault(); onSubmit(ev); }}>
                            <TextField
                                label="Edit channel name"
                                variant="outlined"
                                fullWidth
                                value={editState.newChannelName}
                                onChange={(ev) => onChange(state.channel, ev)}
                                sx={{ mb: 2 }}
                            />
                            <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                                <Button
                                    type="submit"
                                    variant="contained"
                                    disabled={editState.newChannelName ===  channels.get(Number(channelId)).name
                                        || !editState.newChannelName.trim()}
                                    sx={{
                                        backgroundColor: '#28a745',
                                        color: '#fff',
                                        borderRadius: 20,
                                        textTransform: 'none',
                                        mr: 1,
                                        '&:hover': { backgroundColor: '#218838' },
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
                                        ml: 1,
                                        '&:hover': { backgroundColor: '#f8d7da', borderColor: '#dc3545' },
                                    }}
                                >
                                    Cancel
                                </Button>
                            </Box>
                        </form>
                    </Box>
                </Box>
            )}
            {/* Channel Name and Edit Button */}
            {editState.name === 'idle' && state.name === 'displaying' && leaveState.name === 'idle' && (
                <Box textAlign="center" mt={5}>
                    <Typography variant="h4" mt={2} sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', marginTop: 0 }}>
                        {channels.get(Number(channelId)).name}
                        {state.channel.creator.id === user.id && (
                            <Button variant="text" color="primary" onClick={() => onEdit(channels.get(Number(channelId)))} sx={{ ml: 2, textTransform: 'none' }}>
                                <Edit sx={{ mr: 1 }} />
                                Edit
                            </Button>
                        )}
                    </Typography>
                </Box>
            )}
    
            {/* Channel Members List */}
            {state.name === 'displaying' && leaveState.name === 'idle' && (
                <Box textAlign="center" mt={2}>
                    <Typography variant="body2" mt={1}>Number of members: {channelMembers.get(Number(channelId)).length}</Typography>
                    <Typography variant="h6" mt={2}>Channel Members</Typography>
                    <Box 
                        textAlign="center" 
                        mt={2} 
                        sx={{ 
                            maxHeight: '300px', 
                            overflowY: 'auto', 
                            display: 'flex', 
                            justifyContent: 'center',
                            width: '100%',
                        }}
                    >
                    <List 
                        sx={{ 
                            width: '100%', 
                            maxWidth: 400, 
                            overflowY: 'auto', 
                            maxHeight: '300px',
                            bgcolor: 'background.paper', 
                            borderRadius: 2, 
                            boxShadow: 1,
                            '&::-webkit-scrollbar': { width: '8px' }, // Largura da barra de rolagem
                            '&::-webkit-scrollbar-thumb': { backgroundColor: '#888', borderRadius: '4px' }, // Cor do "thumb"
                            '&::-webkit-scrollbar-thumb:hover': { backgroundColor: '#555' } // Cor ao passar o mouse
                        }}
                    >
                        {(() => {
                            return channelMembers.get(Number(channelId)).map((member) => (
                            <ListItem key={member.user.id} sx={{ justifyContent: 'center' }}>
                                <Box
                                    p={1}
                                    border={1}
                                    borderRadius={2}
                                    display="flex"
                                    alignItems="center"
                                    justifyContent="space-between"
                                    bgcolor="#f0f0f0"
                                    width="100%"
                                >
                                    <Typography variant="body1">{member.user.username}</Typography>
                                    <Box display="flex" alignItems="center" gap={1}>
                                        {member.user.id === state.channel.creator.id && (
                                            <Chip label="Creator" size="small" sx={{ backgroundColor: 'blue', color: 'white' }} />
                                        )}
                                        <Chip
                                            label={member.role}
                                            size="small"
                                            sx={{ backgroundColor: member.role === Role.READ_WRITE ? 'green' : '#2f2f2f', color: 'white' }}
                                        />
                                    </Box>
                                </Box>
                            </ListItem>
                        ));
                        })()}
                    </List>
                </Box>    
                    {/* Actions */}
                    
                    <Box textAlign="center" mt={2} display="inline-grid">
                    { state.channel.visibility === 'PRIVATE' && (
                        <Button
                            variant="contained"
                            color="primary"
                            onClick={handleSendInvitation}
                            sx={{ mt: 3, textTransform: 'none' }}
                        >
                            Send Invitation
                        </Button>)}
                        <Button
                            variant="contained"
                            color="error"
                            onClick={() => leaveChannel(state.channel)}
                            sx={{ mt: 3, textTransform: 'none' }}
                        >
                            Leave Channel
                            <ExitToAppIcon sx={{ ml: 1 }} />
                        </Button>
                    </Box>
                </Box>
            )}
            {popUp && (<InvitationOptions onClose={() => setPopUp(false)} channelId={channelId} />)}
        </Box>
    );
    
}
