import * as React from 'react';
import { Box, Button, Card, CardActions, CardContent, Typography } from '@mui/material';
import { useLocation } from "react-router-dom";

export function InvitationOptions() {
    const location = useLocation();
    const { channel, token } = location.state;

    const handleInviteExistingUser = () => {
        console.log('Invite existing user', { channel, token });
        // Adicione aqui a lógica para convidar um usuário existente
    };

    const handleInviteNewUser = () => {
        console.log('Invite new user', { channel, token });
        // Adicione aqui a lógica para convidar um novo usuário
    };

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
            <Card sx={{ width: 400, height: 250, textAlign: 'center' }}>
                <CardContent>
                    <Typography variant="h4" component="h1" gutterBottom fontFamily="Arial">
                        Invitation Options
                    </Typography>
                </CardContent>
                <CardActions sx={{ flexDirection: 'column', gap: 2 }}>
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleInviteExistingUser}
                    >
                        Invite existing user
                    </Button>
                    <Button
                        variant="contained"
                        color="secondary"
                        onClick={handleInviteNewUser}
                    >
                        Invite new user
                    </Button>
                </CardActions>
            </Card>
        </Box>
    );
}