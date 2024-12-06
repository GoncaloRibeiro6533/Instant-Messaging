import * as React from 'react'
import { createRoot } from 'react-dom/client'
import { App } from './App'
import { AuthProvider } from './components/auth/AuthProvider'
import './styles.css' //do not delete this import
import { DataProvider } from './components/data/DataProvider'
import { SseProvider } from './components/sse/SseProvider'

const root = 
    createRoot(document.getElementById("container"))
root.render(<AuthProvider><DataProvider><SseProvider><App/></SseProvider></DataProvider></AuthProvider>)
