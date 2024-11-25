import * as React from 'react'
import { createContext, useState } from 'react'
import { AuthenticatedUser } from '../../domain/AuthenticatedUser'

type AuthContextType = {
    user: AuthenticatedUser | undefined,
    setUser: (user :AuthenticatedUser) => void,
}

export const AuthContext = createContext<AuthContextType>({
    user: undefined,
    setUser: () => {
    },

})

export function AuthProvider({ children }: { children: React.ReactNode }) : React.JSX.Element {
    const [user, setUser] = useState<AuthenticatedUser | undefined>(undefined)
    return (
        <AuthContext.Provider value={{ user, setUser,}}>
            {children}
        </AuthContext.Provider>
    )
}

export function useAuth() {
    const state =  React.useContext(AuthContext)
    return [
        state.user,
        (user: AuthenticatedUser)  => {
            localStorage.setItem('user', JSON.stringify(user))
            state.setUser(user)
        }
    ] as const
}