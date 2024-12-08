import * as React from 'react'
import { createContext, useState } from 'react'
import { User } from '../../domain/User'


type AuthContextType = {
    user: User | undefined,
    setUser: (user :User | undefined) => void,
}

export const AuthContext = createContext<AuthContextType>({
    user: undefined,
    setUser: () => {
    },


})

export function AuthProvider({ children }: { children: React.ReactNode }) : React.JSX.Element {
    const savedUser = localStorage.getItem("user")
    const initialUser = savedUser ? JSON.parse(savedUser) : undefined;
    const [user, setUser] = useState<User | undefined>(initialUser)
    return (
        <AuthContext.Provider value={{ user, setUser }}>
            {children}
        </AuthContext.Provider>
    )
}

export function useAuth() {
    const state =  React.useContext(AuthContext)
    return [
        state.user,
        (user: User | undefined)  => {
            if (user) {
                localStorage.setItem("user", JSON.stringify(user))

            } else {
                localStorage.removeItem("user")
            }
            state.setUser(user)
        }
    ] as const
}