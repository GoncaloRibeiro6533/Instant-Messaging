import * as React from 'react';
import {
    AppBar, Box, Toolbar, Typography, IconButton, Badge, Popper, Paper, ClickAwayListener, List, ListItem, Drawer, ListItemButton, ListItemIcon, ListItemText,
} from "@mui/material";
import { Notifications as NotificationsIcon, AccountCircle, Menu as MenuIcon } from "@mui/icons-material";
import { useNavigate } from "react-router-dom";
import { AuthContext } from '../auth/AuthProvider';
import { useSse } from "../sse/SseProvider";
import { NotificationsList } from "../notifications/notificationsList";
import { LogoutButton } from "../logout/logoutButton";
import GroupAddIcon from '@mui/icons-material/GroupAdd';
import { Home as HomeIcon, Info as InfoIcon, Chat, Add, Close, Login as LoginIcon } from '@mui/icons-material';
import { useData } from '../data/DataProvider';





export default function MenuDrawer() {
    const [isDrawerOpen, setIsDrawerOpen] = React.useState(false);
    const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null); // Anchor for Popper
    const navigate = useNavigate();
    const { invitations } = useData();
    const { user } = React.useContext(AuthContext);
    const [sse, setSse, notifications, deleteNotification, markAsRead] = useSse();
    const menuItems = [
        { label: 'Home', path: '/', icon: <HomeIcon /> },
        { label: 'About', path: '/about', icon: <InfoIcon /> },
    ];

    if (user) {
        menuItems.push(
            { label: 'My Channels', path: '/channels', icon: <Chat /> },
            { label: 'Create Channel', path: '/channels/create', icon: <Add /> },
            {
                label: 'My Channel Invitations',
                path: '/invitations',
                icon: (
                    <Badge badgeContent={invitations.length} color="error">
                        < GroupAddIcon/>
                    </Badge>
                ),
            }
        );
    } else {
        menuItems.push({ label: 'Login', path: '/login', icon: <LoginIcon /> });
    }

    // Toggle Popper
    const handleNotificationsOpen = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl((prev) => (prev ? null : event.currentTarget)); // Toggle properly
        notifications.filter((notification)=> notification.read === false).forEach((notification) => {
            markAsRead(notification.id);
        });
    };

    const handleNotificationsClose = () => {
        setAnchorEl(null); // Ensure it closes
    };

    return (
        <Box sx={{ flexGrow: 1 }}>
            {/* App Bar */}
            <AppBar position="static" sx={{ backgroundColor: "#13161a", color: "#FFFFFF" }}>
                <Toolbar>
                    <IconButton size="large" color="inherit" onClick={() => setIsDrawerOpen(!isDrawerOpen)} sx={{ mr: 2 }}>
                        <MenuIcon />
                    </IconButton>
                    <Typography variant="h6" sx={{ flexGrow: 1 }}>ChImp</Typography>
                    <Drawer
                        anchor="left"
                        open={isDrawerOpen}
                        onClose={() => setIsDrawerOpen(false)}
                        PaperProps={{
                            sx: {
                                width: 250,
                                backgroundColor: '#242424',
                                color: '#FFFFFF',
                                margin: 0,
                                padding: 0,
                                boxSizing: 'border-box',
                                height: '100vh',
                            },
                        }}
                    >
                        <Box
                            role="presentation"
                            onClick={ () => setIsDrawerOpen(false) }
                            onKeyDown={ () => setIsDrawerOpen(false) }
                            sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}
                        >
                            <List sx={{ flexGrow: 1 }}>
                                <ListItem>
                                    <ListItemButton onClick={() => setIsDrawerOpen(false)}
                                                    sx={{ '&:hover': { backgroundColor: '#333' }, }}>
                                        <ListItemIcon sx={{ color: '#FFFFFF' }}>
                                            <Close />
                                        </ListItemIcon>
                                        <ListItemText primary="Close" />
                                    </ListItemButton>
                                </ListItem>
                                {menuItems.map((item, index) => (
                                    <ListItem key={index} disablePadding>
                                        <ListItemButton
                                            onClick={() => navigate(item.path)}
                                            sx={{
                                                '&:hover': { backgroundColor: '#333' },
                                            }}
                                        >
                                            <ListItemIcon sx={{ color: '#FFFFFF' }}>
                                                {item.icon}
                                            </ListItemIcon>
                                            <ListItemText primary={item.label} />
                                        </ListItemButton>
                                    </ListItem>
                                ))}
                            </List>
                            {user && (
                                <ListItem sx={{
                                    marginTop: 'auto',
                                    marginBottom: '60px',
                                    display: 'flex',
                                    justifyContent: 'center',
                                    textAlign: 'center',
                                }}>
                                    <LogoutButton />
                                </ListItem>
                            )}
                        </Box>
                    </Drawer>
                    {user && (
                        <>
                            <IconButton size="large" color="inherit" onClick={() => navigate('/profile')}>
                                <AccountCircle />
                            </IconButton>

                            {/* Notifications Icon with Badge */}
                            <IconButton
                                size="large"
                                color="inherit"
                                onClick={handleNotificationsOpen}
                            >
                                <Badge badgeContent={notifications.filter((notification) => notification.read === false).length} color="error">
                                    <NotificationsIcon />
                                </Badge>
                            </IconButton>

                            {/* Notifications Popper */}
                            <Popper
                                open={Boolean(anchorEl)}
                                anchorEl={anchorEl}
                                placement="bottom-end"
                                sx={{ zIndex: 1300 }}
                            >
                                <ClickAwayListener onClickAway={handleNotificationsClose}>
                                    <Paper
                                        sx={{
                                            width: 350,
                                            maxHeight: 300,
                                            overflowY: "auto",
                                            boxShadow: 3,
                                            p: 2,
                                            borderRadius: 2,
                                            backgroundColor: "white",
                                        }}
                                    >
                                        <NotificationsList />
                                    </Paper>
                                </ClickAwayListener>
                            </Popper>
                        </>
                    )}
                </Toolbar>
            </AppBar>
        </Box>
    );
}
