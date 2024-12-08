import * as React from 'react'
import { Box, Avatar, Typography } from '@mui/material'
import ListItemButton from "@mui/material/ListItemButton"
import { useNavigate, useParams } from 'react-router-dom'
import { ChatBox } from './chatBox/chatBox'
import { useChannel } from './useChannel'
import { getRandomColor } from '../utils/channelLogoColor'
import { CircularProgress } from '@mui/material'
import Snackbar from '@mui/material/Snackbar'
import Button from '@mui/material/Button'
import { useEffect } from 'react'
import { useData } from '../data/DataProvider'

export function Channel() {
    const { channelId } = useParams() 
    const [state, loadChannel] = useChannel()
    const navigate = useNavigate()
    const { channels } = useData()

    useEffect(() => {
         loadChannel(channelId)
    }, [channelId, channels])

   /* useEffect(() => {
        if( state.name === 'loaded' &&
            Array.from(channels.keys()).find(channel => channel.id === state.channel.id).name 
            !== state.channel.name
        ){
                loadChannel(channelId)
            }
   }, [channels])*/

    const handleClick = () => {
        if (state.name === 'loaded') {
            navigate(`/channel/${state.channel.id}`)
        }
    }
    return (
            <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: '600px'
                
             }}>
                 {state.name === 'loading' && (
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexGrow: 1 }}>
                    <CircularProgress size="60px" />
                </Box>
        )}
        {state.name === 'error' && (
            <Box sx={{ flexGrow: 1 }}>
            <Snackbar
            open= {true}
            message={state.message}
            action={
                <Button color='primary' size="large" onClick={() => navigate("/channels")}
                    variant="contained" sx={{ marginLeft: '16px', textColor:'white' }}>
                    CLOSE
                </Button>
            }
            anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
            /></Box>
        )}
        {state.name === 'loaded' && (
             <Box sx={{ flexGrow: 1 }}>
                <ListItemButton onClick={handleClick}>
                    <Avatar sx={{ bgcolor: getRandomColor(state.channel.id), width: 40, height: 40 }}>
                        {state.channel.name.charAt(0).toUpperCase()}
                    </Avatar>
                    <Typography sx={{ marginLeft: '16px' }} variant="h6">
                        {state.channel.name}
                    </Typography>
                </ListItemButton>
                <ChatBox channel={state.channel} /></Box>
            )} </Box>
    )
}
