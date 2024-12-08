import * as React from "react";
import { Outlet } from "react-router-dom";
import { AuthContext } from "../auth/AuthProvider";
import { LoginButton } from "../login/loginButton";
import Logo from "../../../public/logo.png";
import { Typography } from "@mui/material";

export function Home() {
    const { user } = React.useContext(AuthContext);
    return (
        <div style={{textAlign: 'center', marginTop: '50px'}}>
            <img src={Logo} alt="Application Logo" style={{width: '400px', marginBottom: '20px'}}/>
            {user ? (
                <div>
                    <Typography variant="h5">Welcome, {user.username}!</Typography>
                    <Typography variant="h6">Now you can browse your channels!</Typography>
                    <Outlet/>
                </div>
            ) : (
                <div>
                    <Typography variant="h4">Welcome! Please log in.</Typography>
                    <LoginButton/>
                    <Outlet/>
                </div>
            )}
        </div>
    );
}