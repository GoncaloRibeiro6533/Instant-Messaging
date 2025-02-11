
import * as React from "react"
import { services } from "../../../App"
import { Channel } from "../../../domain/Channel"
import {Role} from "../../../domain/Role"
import {useData} from "../../data/DataProvider"
import { useError } from "../../error/errorProvider"
import { useNavigate } from "react-router-dom"
import { useAuth } from "../../auth/AuthProvider"

type State =
    | { name: "idle" }
    | { name: "loading" }
    | { name: "loaded", channels: Map<Number,Channel> }
    | { name: "error", message: string }
    | { name: "stopped" }

type Action =
    | { type: "load" }
    | { type: "success", channels: Map<Number,Channel> }
    | { type: "error", message: string }
    | { type: "stop"}

function reduce(state: State, action: Action): State {
    switch (state.name) {
        case "idle":
            if (action.type === "load") {
                return { name: "loading" }
            }
            if (action.type === "success") {
                return { name: "loaded", channels: action.channels }
            }
            return state
        case "loading":
            if (action.type === "stop") {
                return { name: "stopped" }
            }
            if (action.type === "success") {
                return { name: "loaded", channels: action.channels }
            } else if (action.type === "error") {
                return { name: "error", message: action.message }
            } else {
                return state
            }
        case "loaded":
            if (action.type === "load") {
                return { name: "loading" }
            }
            if (action.type === "success") {
                return { name: "loaded", channels: action.channels }
            }
            return state
        case "error":
            if (action.type === "load") {
                return { name: "loading" }
            } else {
                return state
            }
        case "stopped":
            if (action.type === "load") {
                return { name: "loading" }
            }
            if (action.type === "success") {
                return { name: "loaded", channels: action.channels }
            }
            return state
        default:
            return state
    }
}

export function useChannelList(): [State, onChange: () => void] {
    const [state, dispatch] = React.useReducer(reduce, { name: "idle" })
    const { addChannels, channels, orderByMessages } = useData()
    const [error, setError] = useError()
    const navigate = useNavigate()
    const [user, setUser] = useAuth()
    const { clear } = useData()
    async function loadChannels() {
        if(state.name === "loading") return
        if(channels.size > 0) {
            dispatch({ type: "success", channels })
            return
        }
        dispatch({ type: "load" })
        try {
            const channelsRemote = await services.channelService.getChannelsOfUser(user.id)
            //if(channels.size > 0) {
                addChannels(channelsRemote)
                orderByMessages()
                dispatch({ type: "success", channels })
            /*} else {
                dispatch ({ type: "stop" })
                return
            }*/
        } catch (e) {
            if(e.message === "Not authenticated" || e.message === "Session expired") {
                localStorage.clear()
                setUser(undefined)
                clear()
                navigate('/login')
                return
            }
            if(e.message === "Failed to fetch") {
                //  setError("Failed to connect to the server")
                dispatch({ type: "error", message: "Failed to connect to the server" })
            }
            dispatch({ type: "error", message: e.message })
        }
    }
    return [state, loadChannels]
}

