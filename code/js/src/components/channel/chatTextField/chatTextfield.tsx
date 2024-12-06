import { Box, TextField, Button, Typography, Alert } from "@mui/material";
import * as React from 'react';
import { useTextField } from "./useTextField";
import { Channel } from "../../../domain/Channel";
import { useData } from "../../data/DataProvider";
import { Role } from "../../../domain/Role";


export function ChatTextField(props: { channel: Channel }) {
  const [state, handlers] = useTextField();
  const { channels } = useData();
  const channelContext = Array.from(channels.keys()).find((channel) => channel.id === props.channel.id);
  const role = channels.get(channelContext);
  return (
    (role === Role.READ_WRITE &&
   <form onSubmit={(ev) => handlers.onSubmit(ev, props.channel)} name="chatTextField" id="chatTextField">
     {state.name === "editing" && state.error && (
        <Alert severity="error" sx={{ marginBottom: 2 }}>
          {state.error}
        </Alert>
      )}
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
    </Box></form>) ||
    (role === Role.READ_ONLY &&
    <Box
      sx={{
        display: "flex",
        padding: 2,
        borderTop: "1px solid #ccc",
      }}
    >
      <Typography variant="body1" sx={{ color: "gray" }}>
        You can't send messages to this channel
      </Typography>
      </Box>
    )
  );
}
