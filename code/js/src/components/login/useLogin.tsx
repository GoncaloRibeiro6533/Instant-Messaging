import * as React from "react"
import { useEffect } from "react"
import { useAuth } from "../auth/AuthProvider"
import { useLocation, Navigate } from "react-router-dom"
import { services } from "../../App"
import { AuthenticatedUser } from "../../domain/AuthenticatedUser"

type State = 
    {name: "editing", error?: string, username: string, password: string, } |
    {name: "submitting", username: string, password: string, } |
    {name: "redirecting", username: string, password: string}/*
    {name: string, error: string, username: string, password: string, } */




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
                return { ...state, [action.field]: action.value }
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
        
    }/*
   switch (action.type) {
        case "edit": {
            return { ...state, [action.field]: action.value }
        }
        case "submit": {
            return { name: "submitting", username: state.username, password: state.password, error: "" }
        }
        case "success": {
            return { name: "redirecting", username: state.username, password: state.password, error: "" }
        }
        case "error": {
            return { name: "editing", username: state.username, password: "", error: action.message }
        }
   }*/
}

export function useLogin() : [State, { 
    onSubmit: (ev: React.FormEvent<HTMLFormElement>) => Promise<void>, 
    onChange: (ev: React.FormEvent<HTMLInputElement>) => void 
}] {
   const [state, dispatch] = React.useReducer(reduce, 
        { 
            name: "editing",
            username: "", 
            password: "" ,
            error: ""
        })
    const [, setUser] = useAuth()
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
            if (user) {
                setUser(user)
                dispatch({ type: "success" })
            } else {
                dispatch({ type: "error", message: "Invalid username or password" })
            }
        } catch (e) {
            dispatch({ type: "error", message: e.message })
        }
    }    
    function onChange(ev: React.FormEvent<HTMLInputElement>) { dispatch({ type: "edit", field: ev.currentTarget.name as "username" | "password", value: ev.currentTarget.value }) }
    return[
        state
        ,
        {
            onSubmit: onSubmit,
            onChange: onChange
        }
    ]
    }
    