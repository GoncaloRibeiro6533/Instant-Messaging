import {
    createBrowserRouter, Link, Outlet, RouterProvider, useParams,
} from 'react-router-dom'
import { AuthProvider } from './components/auth/AuthProvider'
import { Home } from './components/home/home'
import { Login } from './components/login/login'
import * as React from 'react'
import { UserServiceMock } from './service/mock/UserServiceMock'
import Service from './service/Service'
import { UserRepoInterface, UserRepo } from './service/mock/repo/UserRepo'
import { Channel } from './domain/Channel'
import { ChannelRepo } from './service/mock/repo/ChannelRepo'
import { MessageRepo } from './service/mock/repo/MessageRepo'
import { ChannelServiceMock} from './service/mock/ChannelServiceMock'
import { MessageServiceMock } from './service/mock/MessageServiceMock'

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

export const userRepoMock = new UserRepo()
export const channelRepoMock = new ChannelRepo()
export const messageRepoMock = new MessageRepo()

export type Repo = {
    userRepo: UserRepo
    channelRepo: ChannelRepo
    messageRepo: MessageRepo
}

export const repo: Repo = {
    userRepo: userRepoMock,
    channelRepo: channelRepoMock,
    messageRepo: messageRepoMock,
}

export const services: Service = {
    userService: new UserServiceMock(repo),
    channelService: new ChannelServiceMock(repo),
    messageService: new MessageServiceMock(repo),
}

export function App() {
    return (
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    );
  }