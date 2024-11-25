import {
    createBrowserRouter, Link, Outlet, RouterProvider, useParams,
} from 'react-router-dom'
import { AuthProvider } from './components/auth/AuthProvider'
import { Home } from './components/home/home'
import { Login } from './components/login/login'
import * as React from 'react'
import { UserServiceMock } from './service/mock/UserServiceMock'
import Service from './service/Service'

const router = createBrowserRouter(
    [
        {
            "path": "/",
            element: 
                <AuthProvider>
                    <Home />
                </AuthProvider>,
            "children": [
                {
                    "path": "/logout",
                    element: <Logout />,
                },
            ]   
        },
        {
            "path": "/login",
            element: 
                <AuthProvider>
                    <Login />
                </AuthProvider>,
            "children": [
                {
                    
                },
            ]   
        },
       
    ]
)

export const services: Service = {
    userService: new UserServiceMock()
}

export function App() {
    return (
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    );
  }