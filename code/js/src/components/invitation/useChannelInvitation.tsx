import * as React from 'react';
import { services } from "../../App";
import { useAuth } from "../auth/AuthProvider";
import {ChannelInvitation} from "../../domain/ChannelInvitation";
import {useData} from "../data/DataProvider";

type State =
    | { name: "idle" }
    | { name: "submitting", channelInvitation: ChannelInvitation }
    | { name: "loading" }
    | { name: "success", channelInvitation: ChannelInvitation }
    | { name: "error", error: string, ChannelInvitation: ChannelInvitation }

type Action =
    | { type: "submit", channelInvitation: ChannelInvitation }
    | { type: "cancel" }
    | { type: "success", channelInvitation: ChannelInvitation }
    | { type: "error", error: string, ChannelInvitation: ChannelInvitation  }

function reduce(state: State, action: Action): State {
    switch (state.name) {
        case "idle":
            switch (action.type) {
                case "submit":
                    return { name: "submitting", channelInvitation: action.channelInvitation };
                default:
                    return state;
            }
        case "submitting":
            switch (action.type) {
                case "success":
                    return { name: "success", channelInvitation: action.channelInvitation };
                case "error":
                    return { name: "error", error: action.error, ChannelInvitation: state.channelInvitation };
                case "cancel":
                    return { name: "idle" };
                default:
                    return state;
            }

        case "loading":
            switch (action.type) {
                case "success":
                    return { name: "success", channelInvitation: action.channelInvitation };
                case "error":
                    return { name: "error", error: action.error, ChannelInvitation: undefined };
                default:
                    return state;
            }
        case "success": {
            return { name: "idle"};
        }

        case "error": {
            switch (action.type) {
                case "submit":
                    return { name: "submitting", channelInvitation: action.channelInvitation };
                case "cancel":
                    return { name: "idle" };
                default:
                    return state
            }
        }
        default:
            return state;

    }
}

export function useChannelInvitation(): [State, {
    onSubmit: (ev: React.FormEvent<HTMLFormElement>, channelInv : ChannelInvitation) => Promise<void>,
    onCancel: () => void
    }] {
    const [state, dispatch] = React.useReducer(reduce, { name: "idle" });
    const [user] = useAuth()

    async function onSubmit(ev: React.FormEvent<HTMLFormElement>, channelInv : ChannelInvitation) {
        ev.preventDefault()
        if (state.name === "submitting") {
            try {
                const channelInvitation =
                    await services.invitationService
                        .createChannelInvitation(channelInv.receiver.id, channelInv.channel.id, channelInv.role);
                dispatch({ type: "success", channelInvitation });
            } catch (error) {
                dispatch({ type: "error", error, ChannelInvitation: state.channelInvitation });
            }
        }
    }
    function onCancel() {
        dispatch({ type: "cancel" })
    }

    return [state, {onSubmit, onCancel}]
}
