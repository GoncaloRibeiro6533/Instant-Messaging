import { Box, TextField, Button } from "@mui/material";
import { useState } from "react";
import * as React from 'react';
import { useTextField } from "./useTextField";

export function ChatTextField() {
  const [state, handlers] = useTextField();

  return (
    <form onSubmit={handlers.onSubmit}>
    <Box
      sx={{
        display: "flex",
        padding: 2,
        borderTop: "1px solid #ccc",
      }}
    >
      <TextField
        fullWidth
        value={state.name === 'editing' ? state.content : ""}
        onChange={handlers.onChange}
        placeholder="Type a message..."
        variant="outlined"
        size="medium"
        multiline // Permite várias linhas
        maxRows={5} // Número máximo de linhas antes da rolagem
        minRows={1} // Altura inicial (opcional)
      />
      <Button
        variant="contained"
        color="primary"
        type= "submit"
        sx={{
          marginLeft: 1,
          alignSelf: "flex-start", // Mantém o botão no topo ou "center" para centralizá-lo
        }}
      >
        Send
      </Button>
    </Box></form>
  );
}
