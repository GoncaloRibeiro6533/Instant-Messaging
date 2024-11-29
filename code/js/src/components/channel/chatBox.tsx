

import * as React from "react";
import { Box, TextField, Button, Paper, Typography } from "@mui/material";
import { Message } from "./message";
import { useState } from "react";
import { Visibility } from "../../domain/Visibility";


// Supondo que temos uma lista de mensagens que são passadas como props ou recuperadas de uma API
const initialMessages = [
    {
        id: 0,
        sender: {
            id: 0,
            username: 'Alice',
            email: ''
        },
        channel: {
            id: 0,
            name: '',
            creator: {
                id: 0,
                username: 'Bob',
                email: ''
            },
            visibility: Visibility.PUBLIC
        },
        content: 'Banana very very very very very llllllllllllllllllllllllllllllllllllllllllll oooooooooooooooooooo nnnnnnnnnnnnnnnnnn    nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn   gggggggggggggg mmmmmmmmmmmmmrrrrrrrrrrrrreeeeeeeeeeeessssssssssssssssssaaaaaaaaaaaaaaggggggggggggggggggggggggeeeeeeeeeee',
        timestamp: new Date()
    },
    {
        id: 1,
        sender: {
            id: 0,
            username: 'Bob',
            email: ''
        },
        channel: {
            id: 0,
            name: '',
            creator: {
                id: 0,
                username: 'Bob',
                email: ''
            },
            visibility: Visibility.PUBLIC
        },
        content: 'Apple '.repeat(10),
        timestamp: new Date()
    }
];

export function ChatBox() {
    const [messages, setMessages] = useState(initialMessages);
    const [newMessage, setNewMessage] = useState("");     

    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: "column",
                height: "80vh",
                width: "100%",
                maxWidth: "600px",
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

           
        </Box>
    );
}
