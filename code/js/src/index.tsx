import * as React from 'react'
import { createRoot } from 'react-dom/client'
import { App } from './App'
import { AuthProvider } from './components/auth/AuthProvider'
import './styles.css' //do not delete this import
import { DataProvider } from './components/data/DataProvider'
import { SseProvider } from './components/sse/SseProvider'
import { ErrorProvider } from './components/error/errorProvider'
import { Error } from './components/error/error'

const root = 
    createRoot(document.getElementById("container"))
root.render(<AuthProvider>
                <DataProvider>
                    <SseProvider>
                        <ErrorProvider>
                        <App/>
                        </ErrorProvider>
                    </SseProvider>
                </DataProvider>
            </AuthProvider>
        )
