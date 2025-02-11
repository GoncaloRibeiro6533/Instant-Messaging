import * as React from 'react'
import { Channel } from '../../domain/Channel'
import { services } from '../../App'
import { useAuth } from '../auth/AuthProvider'
import { useData } from '../data/DataProvider'

type State =
    | { name: 'idle' }
    | { name: 'loading', channelId: number }
    | { name: 'error' , message: string }
    | { name: 'loaded', channel: Channel }

type Action =
    | { type: 'load' ,channelId: number }
    | { type: 'success', channel: Channel, previouslyLoaded: boolean }
    | { type: 'error', message: string }

function reduce(state: State, action: Action): State {
    switch (state.name) {
        case 'idle':
            if (action.type === 'success') {
                return { name: 'loaded', channel: action.channel }
            }
            if (action.type === 'load') {
                return { name: 'loading', channelId: action.channelId }
            }
            return state
        case 'error':
            if (action.type === 'load') {
                return { name: 'loading', channelId: action.channelId }
            }
            if (action.type === 'success') {
                return { name: 'loaded', channel: action.channel }
            }
            return state
        case 'loading':
            if (action.type === 'success') {
                return { name: 'loaded', channel: action.channel }
            }
            if (action.type === 'error') {
                return { name: 'error', message: action.message }
            }
            if (action.type === 'load') {
                return { name: 'loading', channelId: action.channelId }
            }
            return state
        case 'loaded':
            if (action.type === 'load') {
                return { name: 'loading', channelId: action.channelId }
            }
            if (action.type === 'success') {
                return { name: 'loaded', channel: action.channel }
            }
            return state
        default:
            return state
    }
}

export function useChannel(): [
    State,
    (channelId: String) => void
] {
    const [state, dispatch] = React.useReducer(reduce, { name: 'idle' })
    const [auth] = useAuth()
    const { addMessages, messages, channels } = useData()
    const selectedChannelIdRef = React.useRef<number | null>(null)
    async function loadChannel(channelId: string) {
        try {
            const parsedId = parseInt(channelId)
            if (state.name === 'loading' && state.channelId === parsedId) {
                return
            }
            if (isNaN(parsedId)) {
                dispatch({ type: 'error', message: 'Invalid channel ID' })
                return
            }
            selectedChannelIdRef.current = parsedId

            const loadedChannel = channels.get(parsedId)
            if (loadedChannel && messages.get(loadedChannel.id)) {
                if (selectedChannelIdRef.current === parsedId) {
                    dispatch({ type: 'success', channel: loadedChannel, previouslyLoaded: true })
                }
                return
            }
            dispatch({ type: 'load', channelId: parsedId })

            const channel = loadedChannel || await services.channelService.getChannelById(parsedId)
            const retrievedMessages = await services.messageService.getMessages(parseInt(channelId), 30, 0)
            addMessages(channel, retrievedMessages)
            if (selectedChannelIdRef.current === parsedId) {
                dispatch({ type: 'success', channel:channel , previouslyLoaded: false })
            }
        } catch (error: any) {
            const errorMessage = error.message || 'An error occurred'
            dispatch({ type: 'error', message: errorMessage })
        }
    }

    return [state, loadChannel]
}
