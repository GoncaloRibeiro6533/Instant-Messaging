
import * as React from "react"

import { useAuth } from "../auth/AuthProvider"
import { Button } from "@mui/material"
import LogoutIcon from '@mui/icons-material/Logout';

import { useData } from "../data/DataProvider"


function deleteAllCookies() {
    document.cookie.split(';').forEach(cookie => {
        const eqPos = cookie.indexOf('=');
        const name = eqPos > -1 ? cookie.substring(0, eqPos) : cookie;
        document.cookie = name + '=;expires=Thu, 01 Jan 1970 00:00:00 GMT';
    });
}


export function LogoutButton() {
    const [user, setUser] = useAuth()
    const { clear } = useData()
    function logoutHandler() {
        localStorage.clear()
        setUser(undefined)
        clear()
    }
    return (
        <Button
          variant="outlined"
          onClick={logoutHandler}
          sx={{
            color: "#dc3545", // Vermelho chamativo
            borderColor: "#dc3545", // Cor da borda
            borderRadius: 20, // Borda arredondada
            textTransform: "none", // Remove o texto em caixa alta
            fontWeight: "bold",
            paddingX: 3, // Espaçamento horizontal
            paddingY: 1, // Espaçamento vertical
            "&:hover": {
              backgroundColor: "#f8d7da", // Fundo vermelho claro no hover
              borderColor: "#dc3545",
            },
          }}
        >
            <LogoutIcon sx={{ marginRight: 1 }} />
          Log Out
        </Button>
      );
}