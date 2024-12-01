import * as React from "react";
import { Message as MessageType } from "../../domain/Message";
import { User } from "../../domain/User";
import { Box, Avatar, Typography, Paper } from '@mui/material';
import { AuthContext } from "../auth/AuthProvider";

export function Message(props: { message: MessageType }) { 
    const { user } = React.useContext(AuthContext); // Get current user from context
    const isCurrentUser = user.user.username === props.message.sender.username; // Check if message is from current user

    return (
        <Box 
            display="flex" 
            flexDirection="row" 
            alignItems="flex-start" 
            justifyContent={isCurrentUser ? "flex-end" : "flex-start"} // Align message to left or right
            marginBottom={2}
        >
            {/* Avatar of the sender */}
            {props.message.sender.username !== user.user.username &&
            <Avatar 
                src="userImg.png"
                sx={{
                    marginLeft: isCurrentUser ? 0 : 2, 
                    marginRight: isCurrentUser ? 2 : 0,
                    width: 40,
                    height: 40
                }} 
            />
            }
            {/* Message content */}
            <Box maxWidth="75%">
                {/* Message Bubble */}
                <Paper
                    elevation={2}
                    sx={{
                        minWidth: 30,
                        padding: 1.5,
                        marginTop: 0.5,
                        wordWrap: "break-word",
                        backgroundColor: isCurrentUser ? "#4CAF50" : "#E5E5E5", // Green for current user, gray for others
                        color: isCurrentUser ? "white" : "black",
                        borderRadius: 2,
                        maxWidth: "100%",
                        display: "flex",
                        flexDirection: "column",
                        justifyContent: "flex-start", // Ensure the message content is aligned to the top
                        position: "relative", // Ensure timestamp is absolute relative to Paper
                        paddingBottom: "24px", // Add some bottom padding to ensure space for timestamp
                    }}
                >
                      {/* Sender Username */}
                 {props.message.sender.username !== user.user.username && <Typography 
                    variant="body2" 
                    fontWeight="bold" 
                    color={isCurrentUser ? "white" : "text.primary"} 
                    align={isCurrentUser ? "right" : "left"} 
                    sx={{ 
                        marginRight: 2,
                        whiteSpace: 'nowrap',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        maxWidth: '300px',
                        minWidth: '120px',
                        fontSize: '0.9rem',
                        marginBottom: 0.5
                         }}
                >
                    {props.message.sender.username}
                </Typography>}
                {/* Message Content */}
                <Typography variant="body1" align={isCurrentUser ? "right" : "left"}>
                    {props.message.content}
                </Typography>

                    {/* Timestamp positioned at the bottom-right corner */}
                    <Box 
                        sx={{ 
                            position: "absolute", // Position timestamp absolutely within Paper
                            bottom: 4, // Distance from the bottom of the Paper
                            right: 8,  // Distance from the right side of the Paper
                        }}
                    >
                        <Typography 
                            variant="caption" 
                            color="textSecondary" 
                            sx={{ fontSize: '0.8rem'}}
                        >
                            {String(props.message.timestamp.getHours()).padStart(2, "0")}:
                            {String(props.message.timestamp.getMinutes()).padStart(2, "0")}
                        </Typography>
                    </Box>
                </Paper>
            </Box>
        </Box>
    );
};
