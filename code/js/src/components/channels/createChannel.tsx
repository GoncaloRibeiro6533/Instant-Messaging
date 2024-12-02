import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { services } from '../../App';
import { AuthContext, useAuth } from '../auth/AuthProvider';
import { TextField, Button, MenuItem, Box, Typography } from '@mui/material';
import { Visibility } from '../../domain/Visibility';
import { useData } from '../data/DataProvider';
import { Role } from '../../domain/Role';

export function CreateChannel() {
    const [user] = useAuth();
    const { addChannel } = useData()
    const [name, setName] = React.useState('');
    const [visibility, setVisibility] = React.useState(Visibility.PUBLIC);
    const navigate = useNavigate();

    //TODO 
    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        try {
            const result = await services.channelService.createChannel(user.token, name, visibility);
            addChannel(result, Role.READ_WRITE);
            navigate('/channels');
        } catch (error) {
            console.error('Failed to create channel:', error);
        }
    }

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2, maxWidth: 400, margin: 'auto', mt: 4 }}>
            <Typography variant="h6" component="div" sx={{ mb: 2 }}>
                Create a New Channel
            </Typography>
            <TextField
                label="Channel Name"
                variant="outlined"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                fullWidth
            />
            <TextField
                select
                label="Visibility"
                value={visibility}
                onChange={(e) => setVisibility(e.target.value as Visibility)}
                required
                fullWidth
            >
                <MenuItem value={Visibility.PUBLIC}>Public</MenuItem>
                <MenuItem value={Visibility.PRIVATE}>Private</MenuItem>
            </TextField>
            <Button type="submit" variant="contained" color="primary">
                Create Channel
            </Button>
        </Box>
    );
}