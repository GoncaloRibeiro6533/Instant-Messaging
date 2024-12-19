import * as React from "react"
import { useAuth } from "../../auth/AuthProvider"
import { services, useMock } from "../../../App"
import { Channel } from "../../../domain/Channel"
import { useData } from "../../data/DataProvider"

type State =
    { name: "editing", error?: string, content: string}
    | { name: "sending", content: string }
    | { name: "idle"}

type Action =
    { type: "edit", value: string }
    | { type: "send", channel: Channel }
    | { type: "success" }
    | { type: "error", message: string }

function reduce(state: State, action: Action): State {
    switch (state.name) {
        case "editing": {
            if (action.type === "edit") {
                return { ...state, content: action.value, error: undefined }
            } else if (action.type === "send") {
                return { name: "sending", content: state.content }
            } else {
                return state
            }
        }
        case "sending": {
            if (action.type === "success") {
                return { name: "idle" }
            } else if (action.type === "error") {
                return { name: "editing", error: action.message, content: state.content }
            } else {
                return state
            }
        }
        case "idle": {
            if (action.type === "edit") {
                return { name: "editing", content: action.value }
            } else {
                return state
            }
        }
    }
}


export function useTextField() : [State, {
    onSubmit: (ev: React.FormEvent<HTMLFormElement>, channel: Channel) => Promise<void>,
    onChange: (ev: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void,
}] {

    const [state, dispatch] = React.useReducer(reduce,
        {
            name: "idle",
        })
    const [userAuth, setUser] = useAuth()
    const {addMessages} = useData()
    async function onSubmit(ev: React.FormEvent<HTMLFormElement>, channel: Channel) {
        ev.preventDefault()
        if (state.name !== "editing") {
            return
        }
        dispatch({ type: "send", channel: channel })
        try {
            const message = await services.messageService.sendMessage(channel.id, state.content)
            useMock && message && addMessages(channel, [message])
            dispatch({ type: "success" })
        } catch (e) {
            dispatch({ type: "error", message: e.message })
        }
    }

    function onChange(ev: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
        dispatch({ type: "edit", value: ev.target.value })
    }

    return [state, { onSubmit, onChange }]
}