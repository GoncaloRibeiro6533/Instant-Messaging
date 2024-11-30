import * as React from "react"
import { useAuth } from "../auth/AuthProvider"
import { services } from "../../App"

type State = 
    {name: "editing", error?: string, username: string, password: string, } |
    {name: "submitting", username: string, password: string } | 
    {name: "redirecting", username: string, password: string}

type Action = 
    { type: "edit", field: "username" | "password", value: string } 
    | { type: "submit" } 
    | { type: "success" } 
    | { type: "error" , message: string }
    | { type: "redirect" }


function reduce(state: State, action: Action): State {
    switch (state.name) {
        case "editing": {
            if (action.type === "edit") {
                return { ...state, [action.field]: action.value, error: undefined }
            } else if (action.type === "submit") {
                return { name: "submitting", username: state.username, password: state.password }
            } else {
                return state
            }
        }
        case "submitting": {
            if (action.type === "success") {
                return { name: "redirecting", username: state.username, password: "" }
            } else if (action.type === "error") {
                return { name: "editing", error: action.message, username: state.username, password: '' }
            } else {
                return state
            }
        }
        case "redirecting": {
            return state
        }
        
    }
}

export function useLogin() : [State, { 
    onSubmit: (ev: React.FormEvent<HTMLFormElement>) => Promise<void>, 
    onChange: (ev: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void 
}] {
    
   const [state, dispatch] = React.useReducer(reduce, 
        { 
            name: "editing",
            username: "", 
            password: "" ,
            error: undefined
        })
    const [userAuth, setUser] = useAuth()
    async function onSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        if (state.name !== "editing") {
            return
        } 
        dispatch({ type: "submit" })
        const username = state.username
        const password = state.password
        try {
            const user = await services.userService.login(username, password)
            if (user !== undefined) {
                setUser(user)
                dispatch({ type: "success" })
           } else {
                dispatch({ type: "error", message: "Invalid username or password" })
            }
        } catch (e) {
            dispatch({ type: "error", message: e.message })
        }
    }    
    function onChange(ev: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) { dispatch({ type: "edit", field: ev.currentTarget.name as "username" | "password", value: ev.currentTarget.value }) }
    return[
        state,
        {
            onSubmit: onSubmit,
            onChange: onChange
        }
    ]
    }
    