import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { services } from '../../App';
import { useAuth } from '../auth/AuthProvider';
import { TextField, Button, MenuItem, Box, Typography, Paper } from '@mui/material';
import { Visibility } from '../../domain/Visibility';
import { useData } from '../data/DataProvider';
import { Role } from '../../domain/Role';

export function CreateChannel() {
    const [user] = useAuth();
    const { addChannel } = useData();
    const [name, setName] = React.useState('');
    const [visibility, setVisibility] = React.useState(Visibility.PUBLIC);
    const navigate = useNavigate();

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        try {
            const result = await services.channelService.createChannel(name, visibility);
            addChannel(result, Role.READ_WRITE);
            navigate('/channels');
        } catch (error) {
            console.error('Failed to create channel:', error);
        }
    }

    return (
        <Box
            sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                minHeight: '100vh',
                background: "linear-gradient(to right, #F75C64, #F7B731, #26C6DA, #0D0D0D)",
                padding: 3,
            }}
        >
            <Paper
                elevation={4}
                sx={{
                    padding: 4,
                    maxWidth: 420,
                    width: '100%',
                    backgroundColor: '#ffffff', // Fundo branco para contraste
                    boxShadow: '0px 2px 10px rgba(0,0,0,0.1)',
                    textAlign: 'center',
                }}
            >
                <Typography variant="h5" sx={{ fontWeight: 'bold', color: '#333', mb: 2 }}>
                    Create a Channel
                </Typography>
                <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <TextField
                        label="Channel Name"
                        variant="outlined"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        required
                        fullWidth
                        InputProps={{
                            sx: {
                                borderRadius: 0, // Sem bordas arredondadas
                            },
                        }}
                    />
                    <TextField
                        select
                        label="Visibility"
                        value={visibility}
                        onChange={(e) => setVisibility(e.target.value as Visibility)}
                        required
                        fullWidth
                        variant="outlined"
                        InputProps={{
                            sx: {
                                borderRadius: 0, // Sem bordas arredondadas
                            },
                        }}
                    >
                        <MenuItem value={Visibility.PUBLIC}>🌍 Public</MenuItem>
                        <MenuItem value={Visibility.PRIVATE}>🔒 Private</MenuItem>
                    </TextField>
                    <Button
                        type="submit"
                        variant="contained"
                        fullWidth
                        sx={{
                            backgroundColor: '#141a22',
                            color: '#ffffff', // Cor do texto
                            '&:hover': {
                                backgroundColor: '#2b2925', // Cor ao passar o mouse
                            },
                        }}
                    >
                        Create Channel
                    </Button>
                </Box>
            </Paper>
        </Box>
    );
}
