import * as React from "react";
import { Outlet } from "react-router-dom";
import { AuthContext } from "../auth/AuthProvider";
import { LoginButton } from "../login/loginButton";
import Logo from "../../../public/logo.png";
import { Typography, Box, Paper, Button } from "@mui/material";

export function Home() {
    const { user } = React.useContext(AuthContext);

    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                minHeight: "100vh",
                background: "linear-gradient(to right, #F75C64, #F7B731, #26C6DA, #0D0D0D)",
                padding: 3,
            }}
        >
            {/* Card Container */}
            <Paper
                elevation={4}
                sx={{
                    padding: { xs: 3, sm: 4 },
                    borderRadius: 4,
                    maxWidth: "500px",
                    width: "90%",
                    textAlign: "center",
                    backgroundColor: "white",
                    boxShadow: "0 6px 15px rgba(0,0,0,0.2)",
                }}
            >
                {/* Logo */}
                <img
                    src={Logo}
                    alt="Application Logo"
                    style={{
                        width: "100%",
                        maxWidth: "250px",
                        marginBottom: "20px",
                    }}
                />

                {/* Welcome Message */}
                <Typography variant="h4" sx={{ fontWeight: "bold", mb: 1, color: "#333" }}>
                    {user ? `Welcome, ${user.username}!` : "Welcome!"}
                </Typography>
                <Typography variant="body1" sx={{ color: "#666", mb: 3 }}>
                    {user
                        ? "Now you can browse your channels and start chatting!"
                        : "Log in to get started."}
                </Typography>

                {/* Conditional Rendering for Login or Outlet */}
                {user ? <Outlet /> : <LoginButton />}
            </Paper>
        </Box>
    );
}
