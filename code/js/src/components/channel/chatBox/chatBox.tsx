
import * as React from "react"
import { Box, Chip, CircularProgress } from "@mui/material"
import { Message } from "../message"
import { ChatTextField } from "../chatTextField/chatTextfield"
import { useData } from "../../data/DataProvider"
import { Channel } from "../../../domain/Channel"
import { Divider } from "@mui/material"
import { useChatBox } from "./useChatBox"
import { useRef } from "react"

export function ChatBox(props : {channel: Channel}) {
    const [state, loadMessagesHandler] = useChatBox(props.channel)
    const { messages } = useData()
    const channelMessages = messages.get(props.channel.id) || []
    const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
        const container = e.currentTarget
        const previousScroll = container.scrollTop
        const previousScrollHeight = container.scrollHeight
        if (Math.abs(container.scrollHeight -container.clientHeight - Math.abs(container.scrollTop)) <= 100
            && state.name === "displaying") {
            loadMessagesHandler()
            container.scrollTop = previousScroll - (container.scrollHeight - previousScrollHeight)
        }
    }
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
                onScroll={handleScroll}
                sx={{
                    flex: 1,
                    overflowY: "auto",
                    padding: 2,
                    display: "flex",
                    flexDirection: "column-reverse", // Coloca a última mensagem no fundo
                }}
            >
                {(channelMessages.map((message, index) => {
                    const showChip =
                        index === channelMessages.length - 1||
                        message.timestamp.getDay() !== channelMessages[index + 1].timestamp.getDay()
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
                    )
                }))}
                {state.name === 'loading' && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: 2 }}>
                        <CircularProgress size="30px" />
                    </Box>
                )}
                {state.name === 'finished' && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexGrow: 1 }}>
                        <Chip
                            label="Start of the conversation"
                            variant="outlined"
                            sx={{
                                marginTop: 1,
                                backgroundColor: '#171E27',
                                color: '#ffffff', // Cor do texto,
                            }}
                        />
                    </Box>)}
            </Box>
            <ChatTextField channel={props.channel}/>

        </Box>
    )
}
