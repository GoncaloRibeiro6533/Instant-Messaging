
import * as React from "react";
import { services } from "../../App";
import { Channel } from "../../domain/Channel";
import {AuthContext} from "../auth/AuthProvider";


type State =
    | { name: "loading" }
    | { name: "loaded", channels: Channel[] }
    | { name: "error", message: string };

type Action =
    | { type: "load" }
    | { type: "success", channels: Channel[] }
    | { type: "error", message: string };

function reduce(state: State, action: Action): State {
    switch (state.name) {
        case "loading":
            if (action.type === "success") {
                return { name: "loaded", channels: action.channels };
            } else if (action.type === "error") {
                return { name: "error", message: action.message };
            } else {
                return state;
            }
        case "loaded":
        case "error":
            if (action.type === "load") {
                return { name: "loading" };
            } else {
                return state;
            }
        default:
            return state;
    }
}

export function useChannelList(): [State, onChange: () => void] {
    const { user } = React.useContext(AuthContext);
    const [state, dispatch] = React.useReducer(reduce, { name: "loading" });

    async function loadChannels() {
        dispatch({ type: "load" });
        try {
            const channels = await services.channelService.getChannelsOfUser(user.token,user.user.id);
            dispatch({ type: "success", channels });
        } catch (e) {
            dispatch({ type: "error", message: e.message });
        }
    }

    React.useEffect(() => {
        loadChannels().then(r => console.log(r));
    }, [user.user.id]);

    return [state, loadChannels];
}

