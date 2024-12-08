import * as React from 'react'
import { createContext, useState } from 'react'


export type AppError = {
    id: number,
    message: string,
}

type ErrorContextType = {
    error: AppError | undefined,
    setError: (error: AppError | undefined) => void
}

export const ErrorContext = createContext<ErrorContextType>({
    error: undefined,
    setError: () => {},
})

export function ErrorProvider({ children }: { children: React.ReactNode }) : React.JSX.Element {
    const [error, setError] = useState<AppError | undefined>(undefined)

    return (
        <ErrorContext.Provider value={{ error, setError }}>
            {children}
        </ErrorContext.Provider>
    )
}


export function useError() {
    const state =  React.useContext(ErrorContext)
    return [
        state.error,
        (error: string | undefined) => state.setError(
            error ? { id: Math.random(), message: error } : undefined
        )
    ] as const
}