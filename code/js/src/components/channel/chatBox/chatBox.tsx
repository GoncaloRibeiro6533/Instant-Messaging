
import * as React from "react";
import { Box } from "@mui/material";
import { Message } from "../message";
import { ChatTextField } from "../chatTextField/chatTextfield";
import { useData } from "../../data/DataProvider";
import { Channel } from "../../../domain/Channel";

export function ChatBox(props : {channel: Channel}) {
    
    const {messages} = useData();
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
                {messages.get(props.channel.id).map((message, index) => (
                    <Message key={index} message={message} />
                ))}
            </Box>
            <ChatTextField channel={props.channel}/>

        </Box>
    );
}
