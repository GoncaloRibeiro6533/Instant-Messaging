import * as React from 'react';
import { Box, Button, Card, CardActions, CardContent, TextField, InputAdornment, List, ListItem, ListItemText, Paper, ButtonBase, Typography } from '@mui/material';
import { useState, useContext } from 'react';
import { services } from "../../App";
import SearchIcon from "@mui/icons-material/Search";
import { AuthContext } from '../auth/AuthProvider';
import { User } from '../../domain/User';

export function ChannelInvitation() {
    const { user } = React.useContext(AuthContext);
    const [searchTerm, setSearchTerm] = useState('');
    const [searchResults, setSearchResults] = useState<User[]>([]);
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [hoveredUserId, setHoveredUserId] = useState<number | null>(null);
    const [selectedRole, setSelectedRole] = useState<string>('No role selected');

    const handleSearch = async (term: string) => {
        setSearchTerm(term);
        console.log('Search term:', term);
        console.log('User token:', user.token);
        if (term && user.token) {
            try {
                const results = await services.userService.searchByUsername(user.token, term);
                console.log('Search results:', results);
                if (results) {
                    const filteredResults = results.filter(result => result.id !== user.user.id);
                    setSearchResults(filteredResults);
                } else {
                    console.error('No results returned from searchByUsername');
                }
            } catch (e) {
                console.error('Search error:', e.message);
            }
        } else {
            setSearchResults([]);
        }
    };

    const handleUserClick = (user: User) => {
        setSelectedUser(user);
        setSearchResults([]); // Clear search results
        console.log('Selected user:', user);
    };

    const handleRoleClick = (role: string) => {
        setSelectedRole(role);
        console.log('Selected role:', role);
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
            <Card sx={{ width: 400, height: 400, textAlign: 'center', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                <CardContent>
                    <Box sx={{ marginBottom: 0 }}>
                        <TextField
                            label="Search for user"
                            variant="outlined"
                            fullWidth
                            value={searchTerm}
                            onChange={(e) => handleSearch(e.target.value)}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <SearchIcon />
                                    </InputAdornment>
                                ),
                            }}
                        />
                    </Box>
                    {searchResults.length > 0 && (
                        <Paper elevation={3} sx={{ maxHeight: 200, overflow: 'auto', marginBottom: 2 }}>
                            <List>
                                {searchResults.map((user) => (
                                    <ButtonBase
                                        key={user.id}
                                        onClick={() => handleUserClick(user)}
                                        onMouseEnter={() => setHoveredUserId(user.id)}
                                        onMouseLeave={() => setHoveredUserId(null)}
                                        sx={{ width: '100%' }}
                                    >
                                        <ListItem>
                                            <ListItemText
                                                primary={user.username}
                                                sx={{ textDecoration: hoveredUserId === user.id ? 'underline' : 'none' }}
                                            />
                                        </ListItem>
                                    </ButtonBase>
                                ))}
                            </List>
                        </Paper>
                    )}
                </CardContent>
                <CardActions sx={{ flexDirection: 'column', gap: 1, marginBottom: selectedUser ? 1 : 1 }}>
                    <Typography variant="body1" sx={{ marginBottom: selectedUser ? 1 : 2 }}>
                        {selectedUser ? (
                            <>
                                <span style={{ color: 'black', fontWeight: 'bold' }}>User selected:</span>
                                <span style={{ color: 'black', fontStyle: 'italic' }}> {selectedUser.username}</span>
                            </>
                        ) : 'Please select a user by searching its username and the permissions wanted'}
                    </Typography>
                    <Box>
                        <Typography variant="body1" component="span" sx={{ color: 'black', fontWeight: 'bold' }}>
                            Permission selected:
                        </Typography>
                        <Typography variant="body1" component="span" sx={{ color: 'black', fontStyle: 'italic' }}>
                            {selectedRole === 'READ-WRITE' ? ' Can send messages' : selectedRole === 'READ-ONLY' ? ' Can only see conversation' : ' No role selected'}
                        </Typography>
                    </Box>
                    {selectedUser && (
                        <Button
                            variant="contained"
                            color="primary"
                            onClick={() => console.log('Add to channel')}
                        >
                            Add to channel
                        </Button>
                    )}
                </CardActions>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', padding: 2, gap: 2 }}>
                    <Button variant="contained" color="primary" sx={{ textTransform: 'none' }} onClick={() => handleRoleClick('READ-WRITE')}>
                        Can send messages
                    </Button>
                    <Button variant="contained" color="secondary" sx={{ textTransform: 'none' }} onClick={() => handleRoleClick('READ-ONLY')}>
                        Can only see conversation
                    </Button>
                </Box>
            </Card>
        </Box>
    );
}