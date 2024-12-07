import * as React from 'react'
import { createRoot } from 'react-dom/client'
import { App } from './App'
import { AuthProvider } from './components/auth/AuthProvider'
import './styles.css' //do not delete this import
import { DataProvider } from './components/data/DataProvider'
import { SseProvider } from './components/sse/SseProvider'
import { Notification } from './components/notifications/notification'
import { Error } from './components/error/error'
import { ErrorProvider } from './components/error/errorProvider'

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
