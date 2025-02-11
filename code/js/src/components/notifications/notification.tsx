import * as React from "react";
import { Box, Paper, Typography, IconButton, Alert } from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import { useSse, AppNotification } from "../sse/SseProvider";
import { useState, useEffect } from "react";

export function Notification() {
    const [sse, setSse, notifications, deleteNotification, markAsRead] = useSse();
    const [visibleNotifications, setVisibleNotifications] = useState<AppNotification[]>([]);

    useEffect(() => {
        setVisibleNotifications((prev) => {
            // Only add new notifications that are not already visible
            const newNotifications = notifications.filter(
                (n) => !prev.some((v) => v.id === n.id)
            );
            return [...prev, ...newNotifications];
        });
    }, [notifications]);

    useEffect(() => {
        const timeouts = visibleNotifications.map((notification) =>
            setTimeout(() => {
                setVisibleNotifications((prev) =>
                    prev.filter((n) => n.id !== notification.id)
                );
            }, 3000)
        );

        return () => {
            timeouts.forEach(clearTimeout); // Clean up timeouts
        };
    }, [visibleNotifications]);

    // Manually close a notification
    const handleClose = (id: number) => {
        markAsRead(id);
        setVisibleNotifications((prev) => prev.filter((n) => n.id !== id));
    };

    return (
        <>
            {visibleNotifications.slice(0, 3).map((elem, idx) => (
                <Alert
                    key={elem.id}
                    severity="info"
                    sx={{
                        position: "fixed",
                        top: 100 + idx * 60, // Stacking notifications
                        left: "50%",
                        transform: "translateX(-50%)",
                        zIndex: 1300,
                        width: "70%",
                        maxWidth: "90%",
                        boxShadow: 3,
                    }}
                    action={
                        <IconButton
                            size="small"
                            color="inherit"
                            onClick={() => handleClose(elem.id)}
                        >
                            <CloseIcon fontSize="small" />
                        </IconButton>
                    }
                >
                    {elem.message}
                </Alert>
            ))}
        </>
    );
}


/*export function Notification() {
    const [sse, setSse, notifications] = useSse();
    const [open, setOpen] = React.useState(true);
    const [visibleNotifications, setVisibleNotifications] = useState<Notification[]>([]);
    useEffect(() => {
        if (notifications.length > 0) {
            setVisibleNotifications((prev) => [
                ...prev,
                ...notifications.slice(prev.length),
            ]);
        }
    }, [notifications]);
    const handleClose = (index: number) => {
        setVisibleNotifications((prev) => prev.filter((_, i) => i !== index));
    };

    return (
        <>
            {visibleNotifications.slice(0, 3).map((elem, idx) => (
                <Alert severity="info"
                       sx={{
                           position: 'fixed',
                           top: 100 + idx * 60,
                           left: '50%',
                           transform: 'translateX(-50%)',
                           zIndex: 1300,
                           width: '70%',
                           maxWidth: '90%',
                           boxShadow: 3,
                       }}
                       action={
                           <IconButton
                               size="small"
                               color="inherit"
                               onClick={() => handleClose(idx)}
                           >
                               <CloseIcon fontSize="small" />
                           </IconButton>
                       }>
                    {elem.message}
                </Alert>
            ))}
        </>
    )
}*/
    /*return (
        <Modal
            open={open}
            onClose={handleClose}
            sx={{
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
            }}
        >
            <Box
                sx={{
                    padding: 2,
                    maxWidth: 400,
                    backgroundColor: "#fff",
                    border: "1px solid #ddd",
                    borderRadius: 2,
                    boxShadow: 24,
                    position: "relative", 
                }}
            >
                <IconButton
                    onClick={handleClose}
                    sx={{
                        position: "absolute",
                        top: 8,
                        right: 8,
                        color: "#000",
                    }}
                >
                    <CloseIcon />
                </IconButton>
                {notifications.map((notification, index) => (
                    <Paper
                        key={index}
                        sx={{
                            padding: 2,
                            marginBottom: 1,
                            backgroundColor: "#f9f9f9",
                            borderRadius: 2,
                        }}
                    >
                        <Typography variant="body1">{notification.message}</Typography>
                    </Paper>
                ))}
            </Box>
        </Modal>
    );*/

