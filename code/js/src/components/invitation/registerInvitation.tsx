import * as React from 'react';
import { Box, Button, Card, CardActions, CardContent, TextField, Typography, Snackbar } from '@mui/material';
import { useState } from 'react';
import { useLocation, useNavigate } from "react-router-dom";
import { Role } from '../../domain/Role';
import { services } from "../../App";

export function RegisterInvitation() {
    const location = useLocation();
    const { channel, token } = location.state;
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [selectedRole, setSelectedRole] = useState<Role | null>(null);
    const [showPermissionError, setShowPermissionError] = useState(false);
    const [emailError, setEmailError] = useState('');
    const [openSnackbar, setOpenSnackbar] = useState(false);
    const [invitationMessage, setInvitationMessage] = useState('');

    const handleRoleClick = (role: Role) => {
        setSelectedRole(role);
        setShowPermissionError(false);
    };

    const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setEmail(e.target.value);
        setEmailError('');
    };

    const validateEmail = (email: string) => {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(String(email).toLowerCase());
    };

    const handleSendInvitation = async () => {
        if (!email || !validateEmail(email)) {
            setEmailError('Invalid email');
        } else if (!selectedRole) {
            setShowPermissionError(true);
        } else {
            try {
                await services.invitationService.createRegisterInvitation(token, email, channel, selectedRole);
                setInvitationMessage(`Invitation sent to: ${email}`);
                setOpenSnackbar(true);
                navigate(`/channel/${channel.id}`, { state: { invitedEmail: email } });
            } catch (error) {
                console.error('Error sending invitation:', error);
            }
        }
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
            <Card sx={{ width: 400, height: 350, textAlign: 'center', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                <CardContent>
                    <Typography variant="h6" component="div" gutterBottom>
                        Send invitation to (insert email):
                    </Typography>
                    <TextField
                        label="Email"
                        variant="outlined"
                        fullWidth
                        value={email}
                        onChange={handleEmailChange}
                        error={!!emailError}
                        helperText={emailError}
                        sx={{ marginBottom: 2 }}
                    />
                    <Typography variant="body1" component="div" gutterBottom>
                        Select the role wanted:
                    </Typography>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', marginBottom: 2, gap: 1 }}>
                        <Button variant="contained" color="primary" sx={{ textTransform: 'none' }} onClick={() => handleRoleClick(Role.READ_WRITE)}>
                            Can send messages
                        </Button>
                        <Button variant="contained" color="secondary" sx={{ textTransform: 'none' }} onClick={() => handleRoleClick(Role.READ_ONLY)}>
                            Can only see conversation
                        </Button>
                    </Box>
                    <Typography variant="body1" component="div" gutterBottom>
                        <strong>Role selected:</strong> <em>{selectedRole ? (selectedRole === Role.READ_WRITE ? 'Can send messages' : 'Can only see conversation') : 'None'}</em>
                    </Typography>
                    {showPermissionError && (
                        <Typography variant="body2" sx={{ color: 'red', fontStyle: 'italic' }}>
                            Role not selected yet
                        </Typography>
                    )}
                </CardContent>
                <CardActions sx={{ justifyContent: 'center', paddingBottom: 4 }}>
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleSendInvitation}
                        disabled={!email || !validateEmail(email) || !selectedRole}
                    >
                        Send
                    </Button>
                </CardActions>
            </Card>
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