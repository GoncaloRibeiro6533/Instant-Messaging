import * as React from 'react';
import { Box, Button, Card, CardActions, CardContent, TextField, InputAdornment, List, ListItem, ListItemText, Paper, ButtonBase, Typography } from '@mui/material';
import { useState, useContext } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import SearchIcon from "@mui/icons-material/Search";
import { User } from '../../domain/User';
import { Role } from '../../domain/Role';
import { services } from "../../App";
import { AuthContext } from "../auth/AuthProvider";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";

export function ChannelInvitation() {
    const { user } = useContext(AuthContext);
    const location = useLocation();
    const navigate = useNavigate();
    const { channel } = location.state;
    const [searchTerm, setSearchTerm] = useState('');
    const [searchResults, setSearchResults] = useState<User[]>([]);
    const [selectedUser, setSelectedUser] = useState<User | null>(null);
    const [hoveredUserId, setHoveredUserId] = useState<number | null>(null);
    const [selectedRole, setSelectedRole] = useState<Role | null>(null);
    const [showPermissionError, setShowPermissionError] = useState(false);

    const handleSearch = async (term: string) => {
        setSearchTerm(term);
        if (term && user) {
            try {
                const results = await services.userService.searchByUsername(term);
                if (results) {
                    const filteredResults = results.filter((result: { id: any; }) => result.id !== user.id);
                    setSearchResults(filteredResults);
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
        setSearchResults([]);
    };

    const handleRoleClick = (role: Role) => {
        setSelectedRole(role);
    };

    const handleAddToChannel = async () => {
        if (selectedUser && selectedRole) {
            try {
                await services.invitationService.createChannelInvitation(selectedUser.id, channel.id, selectedRole);
                console.log('Channel invitation created');
                navigate(`/channel/${channel.id}`, { state: { invitedUser: selectedUser.username } });
            } catch (e) {
                console.error('Error creating channel invitation:', e.message);
            }
        } else {
            console.error('User or role not selected');
            if (!selectedRole) {
                setShowPermissionError(true);
                setTimeout(() => setShowPermissionError(false), 3000);
            }
        }
    };

    const handleBackClick = () => {
        navigate('/invitation', {state: {channel: channel}});
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
            <Box sx={{ display: 'flex', alignItems: 'center', marginBottom: 2 }}>
                <Button variant="contained" color="primary" onClick={handleBackClick} startIcon={<ArrowBackIcon />}
                        sx={{ textTransform: 'none', margin: 2 }}>
                    Back
                </Button>
            </Box>
            <Card sx={{ width: 400, height: 450, textAlign: 'center', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                <CardContent>
                    <Typography variant="body1" sx={{ marginBottom: 2 }}>
                        Please insert the username of who you want to invite
                    </Typography>
                    <Box sx={{ marginBottom: 2 }}>
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
                        ) : 'Please choose a Role'}
                    </Typography>
                    <Box>
                        <Typography variant="body1" component="span" sx={{ color: 'black', fontWeight: 'bold' }}>
                            Role selected:
                        </Typography>
                        <Typography variant="body1" component="span" sx={{ color: 'black', fontStyle: 'italic' }}>
                            {selectedRole === Role.READ_WRITE ? 'Can send and read messages' : selectedRole === Role.READ_ONLY ? 'Read Only' : 'No role selected'}
                        </Typography>
                    </Box>
                    {showPermissionError && (
                        <Typography variant="body2" sx={{ color: 'red', fontStyle: 'italic' }}>
                            Role not selected yet
                        </Typography>
                    )}
                    {selectedUser && (
                        <Button
                            variant="contained"
                            color="primary"
                            onClick={handleAddToChannel}
                            disabled={!selectedRole}
                        >
                            Add to channel
                        </Button>
                    )}
                </CardActions>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', padding: 2, gap: 2 }}>
                    <Button variant="contained" color="primary" sx={{ textTransform: 'none' }} onClick={() => handleRoleClick(Role.READ_WRITE)}>
                        Read Write Role
                    </Button>
                    <Button variant="contained" color="secondary" sx={{ textTransform: 'none' }} onClick={() => handleRoleClick(Role.READ_ONLY)}>
                        Read Only Role
                    </Button>
                </Box>
            </Card>
        </Box>
    );
}