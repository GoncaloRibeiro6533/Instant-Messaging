import * as React from 'react'
import { createRoot } from 'react-dom/client'
import { App } from './App'
import { AuthProvider } from './components/auth/AuthProvider'

const root = 
    createRoot(document.getElementById("container"))
root.render(<AuthProvider><App/></AuthProvider>)
