import {createBrowserRouter, RouterProvider} from 'react-router-dom'
import { AuthProvider } from './components/auth/AuthProvider'
import { Home } from './components/home/home'
import { Login } from './components/login/login'
import * as React from 'react'
import { UserServiceMock } from './service/mock/UserServiceMock'
import Service from './service/Service'
import { UserRepo } from './service/mock/repo/UserRepo'
import { ChannelRepo } from './service/mock/repo/ChannelRepo'
import { MessageRepo } from './service/mock/repo/MessageRepo'
import { ChannelServiceMock} from './service/mock/ChannelServiceMock'
import { MessageServiceMock } from './service/mock/MessageServiceMock'
import { About } from './components/about/about'
import MenuAppBar from './components/navBar/navigationBar'
//import { ChannelsList} from './components/channels/channelsList'

const router = createBrowserRouter(
    [
        {
            "path": "/",
            element: 
                <AuthProvider>
                    <MenuAppBar/>
                    <Home />
                </AuthProvider>,
            "children": [
               
            ]   
        },
        {
            "path": "/login",
            element: 
                 <AuthProvider> <Login /> </AuthProvider>
         },
         {
            "path": "/about",
            element: 
            <AuthProvider>
                <MenuAppBar/>
                <About />
            </AuthProvider>
        },
        /*{
            "path": "/channels",
            element:
            <AuthProvider>
                <MenuAppBar/>
                <ChannelsList />
            </AuthProvider>
        }

         */


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

export const mockService: Service = {
    userService: new UserServiceMock(repo),
    channelService: new ChannelServiceMock(repo),
    messageService: new MessageServiceMock(repo),
}

export const services: Service = mockService;

export function App() {
    return (
            <RouterProvider router={router} />
    );
  }