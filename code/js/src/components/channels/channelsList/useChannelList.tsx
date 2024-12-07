
import * as React from "react"
import { services } from "../../../App"
import { Channel } from "../../../domain/Channel"
import {AuthContext} from "../../auth/AuthProvider"
import {Role} from "../../../domain/Role"
import {useData} from "../../data/DataProvider"

type State =
    | { name: "idle" }
    | { name: "loading" }
    | { name: "loaded", channels: Map<Channel,Role> }
    | { name: "error", message: string }
    | { name: "stopped" }    

type Action =
    | { type: "load" }
    | { type: "success", channels: Map<Channel,Role> }
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
    const { user } = React.useContext(AuthContext)
    const [state, dispatch] = React.useReducer(reduce, { name: "idle" })
    const { setChannels, channels } = useData()
    async function loadChannels() {
        if(state.name === "loading") return
        if(channels.size > 0) { 
            dispatch({ type: "success", channels })
            return
        }
        dispatch({ type: "load" })
        try {
            const channels = await services.channelService.getChannelsOfUser(user.token,user.user.id)
            if(channels.size > 0) {
            setChannels(channels)
            dispatch({ type: "success", channels })
            } else {
                dispatch ({ type: "stop" })
                return
            }
        } catch (e) {
            dispatch({ type: "error", message: e.message })
        }
    }
    return [state, loadChannels]
}

