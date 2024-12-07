import { Alert, IconButton } from '@mui/material'
import CloseIcon from '@mui/icons-material/Close'
import * as React from 'react'
import { createContext, useState, useEffect } from 'react'
import { AppError, useError } from '../error/errorProvider'



export function Error() {
    const [error, setError] = useError()

    useEffect(() => {
        if (error) {
            const timer = setTimeout(() => {
                setError(undefined)
            }, 10000)
            return () => clearTimeout(timer)
        }
    }, [error, setError])

    return (
        <>
            {error && (
                <Alert severity="error" 
                sx={{ 
                    position: 'fixed', 
                    top: 100, 
                    left: '50%', 
                    transform: 'translateX(-50%)', 
                    zIndex: 1300, 
                    width: '70%', 
                    maxWidth: '90%', 
                    boxShadow: 3, 
                }}
                    action={
                        <IconButton
                            size="small"
                            color="inherit"
                            onClick={() => setError(undefined)}
                        >
                            <CloseIcon fontSize="small" />
                        </IconButton>
                    }>
                    {error.message}
                </Alert>
            )}
        </>
    )
}