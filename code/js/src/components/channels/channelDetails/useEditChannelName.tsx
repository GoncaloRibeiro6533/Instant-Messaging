import * as React from 'react';
import { services } from "../../../App";
import { useAuth } from "../../auth/AuthProvider";
import { Channel } from "../../../domain/Channel";

type State =
    { name: "idle" }
    | { name: "displaying", channel: Channel, newChannelName: string }
    | { name: "editing", error?: string, channel: Channel, newChannelName: string }
    | { name: "submitting", channel: Channel, newChannelName: string }
    | { name: "success", channel: Channel }
    | { name: "error", error: string, channel: Channel };

type Action =
    | { type: "edit", channel: Channel, value: string }
    | { type: "submit" }
    | { type: "cancel" }
    | { type: "success", newChannel: Channel }
    | { type: "error", error: string, channel: Channel };


function reduce(state: State, action: Action): State {
    switch (state.name) {
        case "idle": {
            if (action.type === "edit") {
                return { name: "displaying", channel: action.channel, newChannelName: action.channel.name };
            } else {
                return state;
            }
        }
        case "displaying": {
            if (action.type === "edit") {
                return { name: "editing", channel: action.channel, newChannelName: action.channel.name };
            }
            if (action.type === "cancel") {
                return { name: "idle"};
            }
            return state
        }
        case "editing": {
            if (action.type === "edit") {
                return { ...state, newChannelName: action.value, error: undefined }; // Limpa o erro ao editar
            } else if (action.type === "submit") {
                return { name: "submitting", channel: state.channel, newChannelName: state.newChannelName };
            } else if (action.type === "cancel") {
                return { name: "idle"};
            } else {
                return state;
            }
        }
        case "submitting": {
            if (action.type === "success") {
                return { name: "idle"};
            } else if (action.type === "error") {
                return { name: "editing", channel: action.channel, newChannelName: action.channel.name, error: action.error };
            } else {
                return state;
            }
        }
        case "success": {
            return { name: "idle"};
        }
        case "error": {
            if (action.type === "edit") {
                return { name: "editing", channel: action.channel, newChannelName: action.value, error: undefined };
            } else if (action.type === "cancel") {
                return { name: "idle"};
            } else {
                return state;
            }
        }
        default:
            return state;
    }
}

export function useEditChannelName(): [State, {
    onSubmit: (ev: React.FormEvent<HTMLFormElement>) => Promise<void>,
    onChange: (channel: Channel, ev: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void,
    onEdit: (channel: Channel) => void,
    onCancel: () => void
}]  {
    const [state, dispatch] = React.useReducer(reduce, { name: "idle"})
    const [user, setUserAuth] = useAuth()
    async function onSubmit() {
        if (state.name !== "editing") return
        dispatch({ type: "submit" })
        try {
            const userUpdated = await services.channelService.updateChannelName(user.token, state.channel.id, state.newChannelName)
            dispatch({ type: "success", newChannel: userUpdated })
        } catch (e) {
            dispatch({ type: "error", error: e.message, channel: state.channel })
        }
    }
    function onChange(channel: Channel, ev: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
        dispatch({ type: "edit", channel: channel,value: ev.target.value.trim() })
    }
    function onEdit(channel: Channel) {
        dispatch({ type: "edit", channel: channel, value: channel.name })
    }
    function onCancel() {
        dispatch({ type: "cancel" })
    }
    return [state, { onSubmit, onChange, onEdit, onCancel }]
}


