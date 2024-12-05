
import * as React from "react";
import { Box, Chip } from "@mui/material";
import { Message } from "../message";
import { ChatTextField } from "../chatTextField/chatTextfield";
import { useData } from "../../data/DataProvider";
import { Channel } from "../../../domain/Channel";
import { Message as MessageType } from "../../../domain/Message";
import { Divider } from "@mui/material";

export function ChatBox(props : {channel: Channel}) {
    
    const {messages} = useData();
    const channelMessages: MessageType[] = messages.get(props.channel.id) || [];
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
                {channelMessages.map((message, index) => {
                    const showChip =
                        index === channelMessages.length - 1|| 
                       message.timestamp.getDay() !== channelMessages[index + 1].timestamp.getDay();
                    return (
                        <React.Fragment key={index}>
                        <Message message={message} />
                        {showChip && (
                                <Divider>
                                    <Chip
                                        label={new Intl.DateTimeFormat(['ban', 'id']).format(message.timestamp)}
                                        size="small"
                                        sx={{
                                            marginTop: 1,
                                            width: '100px',
                                            height: '30px',
                                            display: 'block', 
                                            marginLeft: 'auto',
                                            marginRight: 'auto',
                                            textAlign: 'center',
                                            backgroundColor: '#171E27',
                                            color: '#ffffff', // Cor do texto,
                                            alignContent: 'center',
                                        }}
                                    />
                                </Divider>       
                            )}
                </React.Fragment>
            );
        })}

            </Box>
            <ChatTextField channel={props.channel}/>

        </Box>
    );
}
