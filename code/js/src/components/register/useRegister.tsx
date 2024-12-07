import * as React from "react"
import { useAuth } from "../auth/AuthProvider"
import { services } from "../../App"


type State = 
    {name: "editing", error?: string, email:string, username: string, password: string, confirmPassword: string } |
    {name: "submitting", email:string, username: string, password: string } | 
    {name: "redirecting", email:string, username: string, password: string}/*
    {name: string, error: string, username: string, password: string, } */



type Action = 
    { type: "edit", field: "email" | "username" | "password" | "confirmPassword", value: string } 
    | { type: "submit" } 
    | { type: "success" } 
    | { type: "error" , message: string }
    | { type: "redirect" }


function reduce(state: State, action: Action): State {
    switch (state.name) {
        case "editing": {
            if (action.type === "edit") {
                return { ...state, [action.field]: action.value, error: state.error }
            } else if (action.type === "submit") {
                return { 
                    name: "submitting", 
                    email: state.username, 
                    username: state.username, 
                    password: state.password }
                }
                else if (action.type === "error") {
                    return { 
                        name: "editing", 
                        error: action.message, 
                        email: state.email,
                        username: state.username,
                        password: state.password,
                        confirmPassword: state.confirmPassword 
                    }
            } else {
                return state
            }
        }
        case "submitting": {
            if (action.type === "success") {
                return { name: "redirecting", email: state.username, username: state.username, password: "" }
            } else if (action.type === "error") {
                return { 
                    name: "editing",
                     error: action.message, email: "", 
                     username: "", password: "", 
                     confirmPassword: "" }
            } else {
                return state
            }
        }
        case "redirecting": {
            return state
        }
    } 
}


export function useRegister(code: string) : [State, { 
    onSubmit: (ev: React.FormEvent<HTMLFormElement>) => Promise<void>, 
    onChange: (ev: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void 
}] {
   const [state, dispatch] = React.useReducer(reduce, 
        { 
            name: "editing",
            email: "",
            username: "", 
            password: "" ,
            confirmPassword: "",
            error: undefined
        })
    async function onSubmit(ev: React.FormEvent<HTMLFormElement>) {
        ev.preventDefault()
        if (state.name !== "editing") {
            return
        } 
        dispatch({ type: "submit" })
        const email = state.email
        const username = state.username
        const password = state.password
        try {
            const user = await services.userService.register(email, username, password, code)
            if (user !== undefined) {
                dispatch({ type: "success" })
           } else {
                dispatch({ type: "error", message: "Invalid username or password" })
            }
        } catch (e) {
            dispatch({ type: "error", message: e.message })
        }
    }    
    function onChange(ev: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) { 
        dispatch(
            { 
                type: "edit", 
                field: ev.currentTarget.name as "username" | "password" | "email", 
                value: ev.currentTarget.value 
            }) 
       /* if (state.name == 'editing' && state.password !== state.confirmPassword && (state.password != ''  && state.confirmPassword!= '') &&
            ( ev.currentTarget.name === 'confirmPassword' || ev.currentTarget.name === 'password')) {
            dispatch({ type: "error", message: "Passwords do not match" })
        } else if(state.name == 'editing' && state.error != undefined) {
            dispatch({ type: "error", message: undefined})
        }*/
    }
    return[
        state
        ,
        {
            onSubmit: onSubmit,
            onChange: onChange
        }
    ]
    }
    