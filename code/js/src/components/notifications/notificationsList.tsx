import * as React from "react";
import { List, ListItem, ListItemText, Divider, Typography, Box } from "@mui/material";
import { useSse } from "../sse/SseProvider";

export function NotificationsList() {
    const [sse, setSse, notifications] = useSse();
    return (
        <Box sx={{ width: 300, maxHeight: 300, overflowY: "auto" }}>
            <Typography sx={{ padding: 1, fontWeight: "bold" }}>Notifications</Typography>
            <Divider />
            <List>
                {notifications.length > 0 ? (
                    notifications.map((elem, index) => (
                        <ListItem key={index} divider>
                            <ListItemText primary={elem.message} />
                        </ListItem>
                    ))
                ) : (
                    <ListItem>
                        <ListItemText primary="No new notifications" />
                    </ListItem>
                )}
            </List>
        </Box>
    );
}
