import {Channel} from "../../../domain/Channel";
import {ChannelMember} from "../../../domain/ChannelMember";
import * as React from "react"


type State =
    | { name: "displaying", channel: Channel, members: ChannelMember[] }
    | { name: "editing", channel: Channel, members: ChannelMember[], newChannelName: string }
    | { name: "submitting", channel: Channel, members: ChannelMember[], newChannelName: string }
    | { name: "success", channel: Channel, members: ChannelMember[] }
    | { name: "error", error: string, channel: Channel, members: ChannelMember[] };

type Action =
    | { type: "edit" }
    | { type: "changeName", value: string }
    | { type: "submit" }
    | { type: "success", newChannel: Channel }
    | { type: "error", error: string }
    | { type: "leave" };

function reduce(state: State, action: Action): State {
    switch (state.name) {
        case "displaying": {
            if (action.type === "edit") {
                return { name: "editing", channel: state.channel, members: state.members, newChannelName: state.channel.name };
            } else if (action.type === "leave") {
                return { name: "submitting", channel: state.channel, members: state.members, newChannelName: state.channel.name };
            } else {
                return state;
            }
        }
        case "editing": {
            if (action.type === "changeName") {
                return { ...state, newChannelName: action.value };
            } else if (action.type === "submit") {
                return { name: "submitting", channel: state.channel, members: state.members, newChannelName: state.newChannelName };
            } else {
                return state;
            }
        }
        case "submitting": {
            if (action.type === "success") {
                return { name: "success", channel: action.newChannel, members: state.members };
            } else if (action.type === "error") {
                return { name: "error", error: action.error, channel: state.channel, members: state.members };
            } else {
                return state;
            }
        }
        case "success": {
            return state;
        }
        case "error": {
            return state;
        }
        default:
            return state;
    }
}
/*
function useChannelDetails {
    const [state, dispatch] = React.useReducer(reduce, { name: "displaying", channel, members });

    function onChange() {
        if (state.name === "editing") {
            dispatch({ type: "submit" });
        } else if (state.name === "displaying") {
            dispatch({ type: "edit" });
        }
    }

    return [state, onChange];
}

 */