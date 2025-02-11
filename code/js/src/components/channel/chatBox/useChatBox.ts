import { Channel } from "../../../domain/Channel"
import { useAuth } from "../../auth/AuthProvider"
import { useData } from "../../data/DataProvider"
import * as React from 'react'
import { services } from "../../../App"

type State =
    | { name: 'finished', nMessages: number }
    | { name:'displaying', nMessages: number }
    | { name:'loading', nMessages: number }
    | { name:'error', message: string, nMessages: number }

type Action =
    | { type: 'load'}
    | { type: 'error', message: string }
    | { type: 'add', nMessages: number }
    | { type: 'finish'}


function reduce(state: State, action: Action): State {
    switch (state.name) {
        case 'displaying':
            if (action.type === 'load') {
                return { name: 'loading', nMessages: state.nMessages }
            }
            if (action.type === 'error') {
                return { name: 'error', message: action.message, nMessages: state.nMessages }
            }
            return state
        case 'loading':
            if (action.type === 'add' && action.nMessages - state.nMessages === 30) {
                return { name: 'displaying', nMessages: action.nMessages }
            }
            if (action.type === 'add' && action.nMessages - state.nMessages < 30) {
                return { name: 'finished', nMessages: action.nMessages }
            }
            if (action.type === 'error') {
                return { name: 'error', message: action.message, nMessages: state.nMessages }
            }
            if (action.type === 'finish') {
                return { name: 'finished', nMessages:state.nMessages }
            }
            return state
        case 'error':
            return state
    }
}


export function useChatBox(channel: Channel): [
    State,
    loadMessagesHandler: () => void
] {

    const { messages, loadMessages } = useData()
    const channelMessages = messages.get(channel.id).length || 0
    const [state, dispatch] = React.useReducer(reduce, { name: 'displaying', nMessages:  channelMessages })
    const [user] = useAuth()
    async function loadMessagesHandler() {
        if (state.name === 'loading' || state.name === 'finished') return
        dispatch({ type: 'load' })
        try {
            const retrievedMessages = await services.messageService.getMessages(channel.id, 30, state.nMessages)
            if (retrievedMessages.length === 0) {
                dispatch({ type: 'finish' })
                return
            }
            loadMessages(channel, retrievedMessages)
            dispatch({ type: 'add', nMessages: state.nMessages + retrievedMessages.length })
            return
        } catch (e) {
            dispatch({ type: 'error', message: e.message })
        }
    }
    return [state, loadMessagesHandler]
}