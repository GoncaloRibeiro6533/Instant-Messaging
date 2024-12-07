import * as React from 'react';
import { Box, Button, Card, CardContent, CardActions, Typography, Grid, Chip } from '@mui/material';
import { useAuth } from '../auth/AuthProvider';
import { services } from '../../App';
import CheckIcon from '@mui/icons-material/Check';
import ClearIcon from '@mui/icons-material/Clear';
import { useData } from "../data/DataProvider";

export function InvitationsList() {
    const [user] = useAuth();
    const { invitations, setInvitations } = useData();

    React.useEffect(() => {
        const fetchInvitations = async () => {
            if (user) {
                try {
                    const fetchedInvitations = await services.invitationService.getInvitationsOfUser();
                    setInvitations(fetchedInvitations);
                } catch (error) {
                    console.error('Error fetching invitations:', error);
                }
            }
        };
        fetchInvitations();
    }, [user]);

    const handleAccept = async (invitationId: number) => {
        try {
            await services.invitationService.acceptChannelInvitation(invitationId);
            setInvitations(invitations.filter(invitation => invitation.id !== invitationId));
        } catch (error) {
            console.error('Error accepting invitation:', error);
        }
    };

    const handleDecline = async (invitationId: number) => {
        try {
            await services.invitationService.declineChannelInvitation(invitationId);
            setInvitations(invitations.filter(invitation => invitation.id !== invitationId));
        } catch (error) {
            console.error('Error declining invitation:', error);
        }
    };

    return (
        <Box flexDirection="column" sx={{ padding: 2 }}>
            <Typography variant="h4" gutterBottom>
                Invitations
            </Typography>
            {invitations.map(invitation => (
                <Card key={invitation.id} sx={{ marginBottom: 2 }}>
                    <CardContent>
                        <Typography variant="h6">
                            Invited by: {invitation.sender.username}
                        </Typography>
                        <Grid container alignItems="center" spacing={2}>
                            <Grid item>
                                <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                                    Channel: {invitation.channel.name}
                                </Typography>
                            </Grid>
                            <Grid item>
                                <Typography variant="h6" color="textSecondary">
                                    Your role:
                                </Typography>
                            </Grid>
                            <Grid item>
                                <Chip
                                    label={invitation.role}
                                    sx={{
                                        backgroundColor: invitation.role === 'READ_WRITE' ? 'green' : '#2f2f2f',
                                        color: 'white'
                                    }}
                                />
                            </Grid>
                        </Grid>
                    </CardContent>
                    <CardActions>
                        <Button
                            variant="contained"
                            color="success"
                            onClick={() => handleAccept(invitation.id)}
                            startIcon={<CheckIcon />}
                            sx={{ marginRight: 1, textTransform: 'none' }}
                        >
                            Accept
                        </Button>
                        <Button
                            variant="contained"
                            color="error"
                            onClick={() => handleDecline(invitation.id)}
                            startIcon={<ClearIcon />}
                            sx={{ marginRight: 1, textTransform: 'none' }}
                        >
                            Decline
                        </Button>
                    </CardActions>
                </Card>
            ))}
        </Box>
    );
}