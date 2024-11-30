import * as React from 'react';
import { useLogin } from './useLogin';
import { Navigate, useLocation } from 'react-router-dom';
import Logo from '../../../public/logo.png';
import Alert from '@mui/material/Alert';
import { Box, Paper, TextField, Button, Typography } from '@mui/material';
import CircularProgress from '@mui/material/CircularProgress';
import { AuthContext } from "../auth/AuthProvider";


export function Login() {
    const [state, handlers] = useLogin();
    const location = useLocation();
    const { user } = React.useContext(AuthContext)
    if (state.name === 'redirecting' || user != undefined) {
        return <Navigate to={location.state?.source || '/'} replace={true} />;
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
                }}
            >
                {state.name === 'editing' && state.error && (
            <Alert 
                severity="error" 
                sx={{ 
                    padding: 2,
                    marginBottom: 2,
                }}
            >
                {state.error}.
            </Alert>
            )}
                <img src={Logo} alt="Logo" width={250} style={{ marginBottom: 16 }} />
                <Typography variant="h5" component="h1" gutterBottom>
                    Login
                </Typography>

                <form onSubmit={handlers.onSubmit}>
                    <fieldset disabled={state.name !== 'editing'} style={{ border: 'none', padding: 0 }}>
                        <Box display="flex" flexDirection="column" gap={2}>
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
                                disabled={state.name !== 'editing' || !state.username || !state.password}
                                fullWidth
                            >
                                Login
                            </Button>
                        </Box>
                    </fieldset>
                </form>
            </Paper>
        </Box>
    );
}
