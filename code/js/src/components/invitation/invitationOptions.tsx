import * as React from 'react';
import { Box, Button, Typography, Modal, Card, CardActions, CardContent } from '@mui/material';
import { useLocation, useNavigate } from "react-router-dom";

export function InvitationOptions(props: { onClose: () => void, channelId: string }) {
    const [open, setOpen] = React.useState(true);
    const navigate = useNavigate();


    const handleInvitationExistingUser = () => {
        navigate(`/invitation/channel/${props.channelId}`);	
        setOpen(false);
        props.onClose();
    };

    const handleInvitationNewUser = () => {
        navigate(`/invitation/register/${props.channelId}`);
        setOpen(false);
        props.onClose();

    };
   
    return (
        <Modal open={open} onClose={()=> props.onClose} aria-labelledby="invitation-options-title">
            <Box
                display="flex"
                alignItems="center"
                justifyContent="center"
                height="100vh"
            >
                <Card
                    sx={{
                        width: 400,
                        padding: 3,
                        textAlign: "center",
                        borderRadius: "12px",
                        boxShadow: "0px 5px 15px rgba(0, 0, 0, 0.2)",
                        backgroundColor: "#fff"
                    }}
                >
                    <CardContent>
                        <Typography 
                            id="invitation-options-title"
                            variant="h4"
                            component="h1"
                            gutterBottom
                            sx={{
                                fontFamily: "Arial",
                                fontWeight: "bold",
                                color: "#333"
                            }}
                        >
                            Invitation Options
                        </Typography>
                    </CardContent>
                    <CardActions sx={{ flexDirection: 'column', gap: 2 }}>
                            <Button
                                variant="contained"
                                color="primary"
                                onClick={handleInvitationExistingUser}
                                sx={{
                                    width: "90%",
                                    borderRadius: "20px",
                                    fontSize: "1rem",
                                    fontWeight: "bold",
                                    backgroundColor: "#007bff",
                                    '&:hover': {
                                        backgroundColor: "#0056b3",
                                    }
                                }}
                            >
                                Invite Existing User
                            </Button>
                        <Button
                            variant="contained"
                            color="secondary"
                            onClick={handleInvitationNewUser}
                            sx={{
                                width: "90%",
                                borderRadius: "20px",
                                fontSize: "1rem",
                                fontWeight: "bold",
                                backgroundColor: "#28a745",
                                '&:hover': {
                                    backgroundColor: "#218838",
                                }
                            }}
                        >
                            Invite New User
                        </Button>
                        <Button
                            variant="outlined"
                            color="error"
                            onClick={() => {
                                props.onClose()
                                setOpen(false)
                               }
                            }
                            sx={{
                                width: "90%",
                                borderRadius: "20px",
                                fontSize: "1rem",
                                fontWeight: "bold",
                                mt: 2
                            }}
                        >
                            Cancel
                        </Button>
                    </CardActions>
                </Card>
            </Box>
        </Modal>
    );
}
