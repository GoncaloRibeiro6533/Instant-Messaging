import * as React from 'react';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import InboxIcon from '@mui/icons-material/Inbox';
import LoginIcon from '@mui/icons-material/Login';
import MenuIcon from '@mui/icons-material/Menu';
import Drawer from '@mui/material/Drawer';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import HomeIcon from '@mui/icons-material/Home';
import InfoIcon from '@mui/icons-material/Info';
import AccountCircle from '@mui/icons-material/AccountCircle';
import { useNavigate } from 'react-router-dom';
import { Add, Chat, Close } from "@mui/icons-material";
import { AuthContext } from '../auth/AuthProvider';
import { LogoutButton } from '../logout/logoutButton';
import NotificationAddIcon from '@mui/icons-material/NotificationAdd';

export default function MenuDrawer() {
    const [isDrawerOpen, setIsDrawerOpen] = React.useState(false);
    const navigate = useNavigate();
    const { user } = React.useContext(AuthContext);

    const toggleDrawer = (open: boolean) => (event: React.KeyboardEvent | React.MouseEvent) => {
        if (
            event.type === 'keydown' &&
            ((event as React.KeyboardEvent).key === 'Tab' ||
                (event as React.KeyboardEvent).key === 'Shift')
        ) {
            return;
        }
        setIsDrawerOpen(open);
    };

    const menuItems = [
        { label: 'Home', path: '/', icon: <HomeIcon /> },
        { label: 'About', path: '/about', icon: <InfoIcon /> },
    ];

    if (user) {
        menuItems.push(
            { label: 'Channels List', path: '/channels', icon: <Chat /> },
            { label: 'Create Channel', path: '/createChannel', icon: <Add /> },
            { label: 'My Channel Invitations', path: '/invitations', icon: <NotificationAddIcon /> }
        );
    } else {
        menuItems.push({ label: 'Login', path: '/login', icon: <LoginIcon /> });
    }

    return (
        <Box sx={{flexGrow: 1, margin: 0, padding: 0 }}>
            <AppBar position="static"
                    sx={{
                        backgroundColor: '#13161a',
                        color: '#FFFFFF',
                    }}>
                <Toolbar>
                    {/* Menu Icon to toggle Drawer */}
                    <IconButton
                        size="large"
                        edge="start"
                        color="inherit"
                        aria-label="menu"
                        onClick={toggleDrawer(true)}
                        sx={{ mr: 2 }}
                    >
                        <MenuIcon />
                    </IconButton>
                    {/* Title */}
                    <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                        ChImp
                    </Typography>

                    {/* Account Circle */}
                    <IconButton
                        size="large"
                        aria-label="account of current user"
                        sx={{ color: '#FFFFFF' }}
                        onClick={() => navigate('/profile')}
                    >
                        <AccountCircle />
                    </IconButton>
                </Toolbar>
            </AppBar>

            {/* Drawer Component */}
            <Drawer
                anchor="left"
                open={isDrawerOpen}
                onClose={toggleDrawer(false)}
                PaperProps={{
                    sx: {
                        width: 250,
                        backgroundColor: '#242424',
                        color: '#FFFFFF',
                        margin: 0, // Remove margens
                        padding: 0, // Remove preenchimentos
                        boxSizing: 'border-box', // Garante alinhamento
                        height: '100vh', // Ocupa altura total
                    },
                }}
            >
                <Box
                    role="presentation"
                    onClick={toggleDrawer(false)}
                    onKeyDown={toggleDrawer(false)}
                    sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}  // Ensure the Box takes up full height
                >

                    {/* Menu items */}
                    <List sx={{ flexGrow: 1 }}>
                        <ListItem>
                            <ListItemButton onClick={toggleDrawer(false)}
                                            sx={{ '&:hover': { backgroundColor: '#333' },}}>
                                <ListItemIcon sx={{color: '#FFFFFF'}}>
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
                                    <ListItemIcon sx={{color: '#FFFFFF'}}>
                                        {item.icon}
                                    </ListItemIcon>
                                    <ListItemText primary={item.label} />
                                </ListItemButton>
                            </ListItem>
                        ))}
                    </List>

                    {/* Logout Button */}
                    <ListItem sx={{
                        marginTop: 'auto',
                        marginBottom: '60px',
                        display: 'flex',
                        justifyContent: 'center',
                        textAlign: 'center',
                    }}>
                        <LogoutButton />
                    </ListItem>
                </Box>
            </Drawer>
        </Box>
    );
}
