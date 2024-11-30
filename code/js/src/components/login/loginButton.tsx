import * as React from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@mui/material";

export function LoginButton() {
    const navigate = useNavigate();
    return (
        <Button variant="contained" color="primary" onClick={() => navigate("/login")}>
            Login
        </Button>
    );
}