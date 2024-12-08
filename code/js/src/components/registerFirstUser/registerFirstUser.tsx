import * as React from 'react';
import { Navigate } from 'react-router-dom';
import Alert from '@mui/material/Alert';
import { Box, Paper, TextField, Button, Typography } from '@mui/material';
import CircularProgress from '@mui/material/CircularProgress';
import { useRegisterFirstUser } from './useRegisterFirstUser';
import Logo from '../../../public/logo.png';

export function RegisterFirstUser() {
    const [state, handlers] = useRegisterFirstUser()
    if (state.name === 'redirecting') {
        return <Navigate to={'/login'} replace={true} />;
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
            <Paper
                elevation={3}
                sx={{
                    padding: 4,
                    maxWidth: 400,
                    width: '100%',
                    textAlign: 'center',
                }}>
                {state.name === 'editing' && state.error && (
                    <Alert
                        severity="error"
                        sx={{
                            padding: 2,
                            marginBottom: 2,
                        }}>
                        {state.error}.
                    </Alert>
                )}
                <img src={Logo} alt="Logo" width={250} style={{ marginBottom: 16 }} />
                <Typography variant="h5" component="h1" gutterBottom>
                    Register
                </Typography>

                <form onSubmit={handlers.onSubmit}>
                    <fieldset disabled={state.name !== 'editing'} style={{ border: 'none', padding: 0 }}>
                        <Box display="flex" flexDirection="column" gap={2}>
                            <TextField
                                id="email"
                                label="Email"
                                type="text"
                                name="email"
                                value={state.email}
                                onChange={handlers.onChange}
                                fullWidth
                                variant="outlined"
                            />
                            <TextField
                                id="username"
                                label="Username"
                                type="text"
                                name="username"
                                value={state.username}
                                onChange={handlers.onChange}
                                fullWidth
                                variant="outlined"
                            />
                            <TextField
                                id="password"
                                label="Password"
                                type="password"
                                name="password"
                                value={state.password}
                                onChange={handlers.onChange}
                                fullWidth
                                variant="outlined"
                            />
                            <TextField
                                id="confirmPassword"
                                label="Confirm Password"
                                type="password"
                                name="confirmPassword"
                                value={ state.name === 'editing' ? state.confirmPassword : ''}
                                onChange={handlers.onChange}
                                fullWidth
                                variant="outlined"
                            />
                            <Button
                                variant="contained"
                                sx={{
                                    backgroundColor: '#100c08',
                                    color: '#ffffff', // Cor do texto
                                    '&:hover': {
                                        backgroundColor: '#2b2925', // Cor ao passar o mouse
                                    },
                                }}
                                type="submit"
                                disabled={state.name !== 'editing' || !state.username || !state.password || !state.email || !state.confirmPassword
                                    && state.password !== state.confirmPassword
                                }
                                fullWidth
                            >
                                Register
                            </Button>
                        </Box>
                    </fieldset>
                </form>
            </Paper>
        </Box>
    );
}
