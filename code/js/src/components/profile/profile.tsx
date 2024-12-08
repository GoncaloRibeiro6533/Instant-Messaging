import * as React from 'react';
import { Box, Typography, Avatar, Button, Paper, TextField, Alert } from '@mui/material';
import { useProfile } from './useProfile';
import CircularProgress from '@mui/material/CircularProgress';

export function Profile() {
    const [state, handlers] = useProfile();

    return (
        <Box
            sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                minHeight: '100vh',
                backgroundColor: '#f4f4f4',
            }}>
            <Paper
                elevation={3}
                sx={{
                    padding: 4,
                    width: 320,
                    textAlign: 'center',
                    borderRadius: 3,
                }}>
                {state.name === 'submitting' && (
                    <Box
                        position="absolute"
                        top={0}
                        left={0}
                        width="100%"
                        height="100%"
                        display="flex"
                        alignItems="center"
                        justifyContent="center"
                        bgcolor="rgba(255, 255, 255, 0.8)" // Fundo semitransparente
                        zIndex={10} // Eleva o overlay acima de tudo
                    >
                        <CircularProgress size="60px" />
                    </Box>
                )}
                {/* Error Alert */}
                {(state.name === 'editing' || state.name === 'error') && state.error && (
                    <Alert severity="error" sx={{ marginBottom: 2 }}>
                        {state.error}
                    </Alert>
                )}
                {/* Avatar */}
                <Avatar
                    src="userImg.png"
                    sx={{
                        width: 80,
                        height: 80,
                        margin: '0 auto 16px',
                    }}
                />
                {/* User Info */}
                {state.name === 'displaying' ? (
                        <>
                            <Typography variant="h6" fontWeight="bold">
                                {state.user.username}
                            </Typography>
                            <Typography
                                variant="body2"
                                color="text.secondary"
                                sx={{ marginBottom: 2 }}
                            >
                                {state.user.email}
                            </Typography>
                            <Button
                                onClick={handlers.onEdit}
                                variant="contained"
                                sx={{
                                    backgroundColor: '#007bff',
                                    color: '#fff',
                                    borderRadius: 20,
                                    textTransform: 'none',
                                    marginBottom: 3,
                                    '&:hover': {
                                        backgroundColor: '#0056b3',
                                    },
                                }}>
                                Edit Username
                            </Button>
                        </>
                    ) :
                    ((state.name === 'editing'
                            || state.name === 'submitting')
                        && (
                            <form
                                onSubmit={(ev) => {
                                    ev.preventDefault(); // Previne a submissão padrão do formulário
                                    handlers.onSubmit(ev);
                                }}>
                                <TextField
                                    label="Edit Username"
                                    variant="outlined"
                                    fullWidth
                                    value={state.newUsername}
                                    onChange={handlers.onChange}
                                    sx={{ marginBottom: 2 }}
                                />
                                <Box
                                    sx={{
                                        display: 'flex',
                                        justifyContent: 'space-between',
                                    }}>
                                    <Button
                                        type="submit"
                                        variant="contained"
                                        disabled={state.newUsername === state.user.username || !state.newUsername.trim()}
                                        sx={{
                                            backgroundColor: '#28a745',
                                            color: '#fff',
                                            borderRadius: 20,
                                            textTransform: 'none',
                                            marginRight: 1,
                                            '&:hover': {
                                                backgroundColor: '#218838',
                                            },
                                        }}>
                                        Save
                                    </Button>
                                    <Button
                                        onClick={handlers.onCancel}
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
                                        }}>
                                        Cancel
                                    </Button>
                                </Box>
                            </form>
                        ))}
            </Paper>
        </Box>
    );
}
