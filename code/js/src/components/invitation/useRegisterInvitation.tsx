import * as React from 'react';
import { services } from "../../App";
import { useAuth } from "../auth/AuthProvider";
import {useData} from "../data/DataProvider";
import {RegisterInvitation} from "../../domain/RegisterInvitation";

type State =
    | { name: "idle" }
    | { name: "submitting", registerInvitation: RegisterInvitation }
    | { name: "loading" }
    | { name: "success", registerInvitation: RegisterInvitation }
    | { name: "error", error: string, registerInvitation: RegisterInvitation }

type Action =
    | { type: "submit", registerInvitation: RegisterInvitation }
    | { type: "cancel" }
    | { type: "success", registerInvitation: RegisterInvitation }
    | { type: "error", error: string, registerInvitation: RegisterInvitation  }

function reduce(state: State, action: Action): State {
    switch (state.name) {
        case "idle":
            switch (action.type) {
                case "submit":
                    return { name: "submitting", registerInvitation: action.registerInvitation };
                default:
                    return state;
            }
        case "submitting":
            switch (action.type) {
                case "success":
                    return { name: "success", registerInvitation: action.registerInvitation };
                case "error":
                    return { name: "error", error: action.error, registerInvitation: state.registerInvitation };
                case "cancel":
                    return { name: "idle" };
                default:
                    return state;
            }

        case "loading":
            switch (action.type) {
                case "success":
                    return { name: "success", registerInvitation: action.registerInvitation };
                case "error":
                    return { name: "error", error: action.error, registerInvitation: undefined };
                default:
                    return state;
            }
        case "success": {
            return { name: "idle"};
        }

        case "error": {
            switch (action.type) {
                case "submit":
                    return { name: "submitting", registerInvitation: action.registerInvitation };
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

export function useRegisterInvitation(): [State, {
    onSubmit: (ev: React.FormEvent<HTMLFormElement>, registerInv:RegisterInvitation) => Promise<void>,
    onCancel: () => void
}] {
    const [state, dispatch] = React.useReducer(reduce, { name: "idle" });
    const [user] = useAuth()

    async function onSubmit(ev: React.FormEvent<HTMLFormElement>, registerInv:RegisterInvitation) {
        ev.preventDefault()
        if (state.name === "submitting") {
            try {
                await services.invitationService
                    .createRegisterInvitation(user.token, registerInv.email, registerInv.channel.id, registerInv.role);
                dispatch({ type: "success", registerInvitation: registerInv });
            } catch (error) {
                dispatch({ type: "error", error, registerInvitation: state.registerInvitation });
            }
        }
    }
    function onCancel() {
        dispatch({ type: "cancel" })
    }

    return [state, {onSubmit, onCancel}]
}
