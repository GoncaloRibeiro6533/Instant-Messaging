

import * as React from "react";
import { Box, TextField, Button, Paper, Typography } from "@mui/material";
import { Message } from "../message";
import { useState } from "react";
import { Visibility } from "../../../domain/Visibility";
import { ChatTextField } from "../chatTextField/chatTextfield";
import { repo } from "../../../App";


export function ChatBox() {
    const [messages, setMessages] = useState(repo.messageRepo.messages);
    const [newMessage, setNewMessage] = useState("");     

    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: "column",
                height: "80vh",
                width: "100%",
                margin: "0 auto",
                backgroundColor: "#fafafa",
            }}
        >
            {/* Área de mensagens */}
            <Box
                sx={{
                    flex: 1,
                    overflowY: "auto",
                    padding: 2,
                    display: "flex",
                    flexDirection: "column-reverse", // Coloca a última mensagem no fundo
                }}
            >
                {messages.map((message, index) => (
                    <Message key={index} message={message} />
                ))}
            </Box>
            <ChatTextField/>
           
        </Box>
    );
}
