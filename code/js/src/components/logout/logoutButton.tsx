
import * as React from "react"

import { useAuth } from "../auth/AuthProvider"
import { Button } from "@mui/material"

export function LogoutButton() {
    const [user, setUser] = useAuth()
    function logoutHandler() {
        localStorage.clear()
        setUser(undefined)
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
          Log Out
        </Button>
      );
}