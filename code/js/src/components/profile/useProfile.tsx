import * as React from "react"
import { AuthContext, useAuth } from "../auth/AuthProvider"
import { services } from "../../App"
import { User } from "../../domain/User"

type State =
    { name: "displaying", user: User }
    | { name: "editing", error?: string, user: User, newUsername: string } 
    | { name: "submitting", user: User, newUsername: string } 
    | { name: "success", user: User }
    | { name: "error", error: string, user: User };

type Action =
    | { type: "edit", value: string } 
    | { type: "submit" }
    | { type: "cancel" }
    | { type: "success", newUser: User }
    | { type: "error", error: string, user: User };


    function reduce(state: State, action: Action): State {
        switch (state.name) {
            case "displaying": {
                if (action.type === "edit") {
                    return { name: "editing", user: state.user, newUsername: state.user.username };
                } else {
                    return state;
                }
            }
            case "editing": {
                if (action.type === "edit") {
                    return { ...state, newUsername: action.value, error: undefined }; // Limpa o erro ao editar
                } else if (action.type === "submit") {
                    return { name: "submitting", user: state.user, newUsername: state.newUsername };
                } else if (action.type === "cancel") {
                    return { name: "displaying", user: state.user };
                } else {
                    return state;
                }
            }
            case "submitting": {
                if (action.type === "success") {
                    return { name: "displaying", user: action.newUser };
                } else if (action.type === "error") {
                    // Retorna ao estado de edição com a mensagem de erro
                    return { name: "editing", user: action.user, newUsername: action.user.username, error: action.error };
                } else {
                    return state;
                }
            }
            case "success": {
                // Simplesmente retorna ao estado de exibição
                return { name: "displaying", user: state.user };
            }
            case "error": {
                // Permite que o usuário volte para edição ao corrigir o erro
                if (action.type === "edit") {
                    return { name: "editing", user: state.user, newUsername: action.value, error: undefined };
                } else if (action.type === "cancel") {
                    return { name: "displaying", user: state.user };
                } else {
                    return state;
                }
            }
            default:
                return state;
        }
    }

export function useProfile(): [State, { 
    onSubmit: (ev: React.FormEvent<HTMLFormElement>) => Promise<void>, 
    onChange: (ev: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void, 
    onEdit: () => void,
    onCancel: () => void
    }]  {
    const { user } = React.useContext(AuthContext)
    const [state, dispatch] = React.useReducer(reduce, { name: "displaying", user: user.user })
    const [userAuth, setUserAuth] = useAuth()
    async function onSubmit() {
        if (state.name !== "editing") return
        dispatch({ type: "submit" })
        try {
            const userUpdated = await services.userService.updateUsername(user.token, state.newUsername)
            setUserAuth({token: user.token, user: userUpdated})
            dispatch({ type: "success", newUser: userUpdated })
        } catch (e) {
            dispatch({ type: "error", error: e.message, user: user.user })
        }
    }
    function onChange(ev: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) { 
        dispatch({ type: "edit", value: ev.target.value })
    }
    function onEdit() {
        dispatch({ type: "edit", value: user.user.username })
    }
    function onCancel() {
        dispatch({ type: "cancel" })
    }
    return [state, { onSubmit, onChange, onEdit, onCancel }]
}


