import * as React from 'react';
import { Box, Button, Card, CardActions, CardContent, Typography } from '@mui/material';
import { useLocation, useNavigate } from "react-router-dom";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";

export function InvitationOptions() {
    const location = useLocation();
    const { channel, token } = location.state;
    const navigate = useNavigate();

    const handleInvitationExistingUser = () => {
        console.log('Invite existing user', { channel, token });
        navigate('/invitation/channel', { state: { channel, token } })
    }

    const handleInvitationNewUser = () => {
        console.log('Invite new user', { channel, token });
        navigate('/invitation/register', { state: { channel, token } })
    }

    const handleBackClick = () => {
        navigate('/channel/' + channel.id)
    }

    return (
        <Box
            display="flex"
            flexDirection="column"
            alignItems="center"
            justifyContent="center"
            height="100vh"
            bgcolor="#f5f5f5"
            padding={2}
        >
            <Box sx={{ display: 'flex', alignItems: 'center', marginBottom: 2 }}>
                <Button variant="contained" color="primary" onClick={handleBackClick} startIcon={<ArrowBackIcon />}
                        sx={{ textTransform: 'none', margin: 2 }}>
                    Back
                </Button>
            </Box>

            <Card sx={{ width: 400, height: 250, textAlign: 'center' }}>
                <CardContent>
                    <Typography variant="h4" component="h1" gutterBottom fontFamily="Arial">
                        Invitation Options
                    </Typography>
                </CardContent>
                <CardActions sx={{ flexDirection: 'column', gap: 2 }}>
                    {channel.visibility !== 'PUBLIC' && (
                        <Button
                            variant="contained"
                            color="primary"
                            onClick={handleInvitationExistingUser}
                        >
                            Invite existing user to this channel
                        </Button>
                    )}
                    <Button
                        variant="contained"
                        color="secondary"
                        onClick={handleInvitationNewUser}
                    >
                        Invite new user to this channel
                    </Button>
                </CardActions>
            </Card>
        </Box>
    );
}