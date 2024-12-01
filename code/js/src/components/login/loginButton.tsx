import * as React from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@mui/material";

export function LoginButton() {
    const navigate = useNavigate();
    return (
        <Button sx={{
            backgroundColor: '#171E27',
            color: '#ffffff', // Cor do texto
            '&:hover': {
                backgroundColor: '#2b2925', // Cor ao passar o mouse
            },
        }} variant="contained" onClick={() => navigate("/login")}>
            Login
        </Button>
    );
}