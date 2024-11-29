import {
    createBrowserRouter, Link, Outlet, RouterProvider, useParams,
} from 'react-router-dom'
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
import { Register } from './components/register/register'
import { AuthRequire } from './components/auth/AuthRequire'
//import { ChannelsList} from './components/channels/channelsList'
import { Profile } from './components/profile/profile'
import { Message } from './components/channel/message'
import { Visibility } from './domain/Visibility'
import { Chat } from '@mui/icons-material'
import { ChatBox } from './components/channel/chatBox'

const router = createBrowserRouter(
    [
        {
            "path": "/",
            element: 
                    <><MenuAppBar /><Home /></>,
            "children": [
               
            ]   
        },
        {
            "path": "/register/:id",
            element: <Register/>
         },
        {
            "path": "/login",
            element:
                 <Login />
         },
         {
            "path": "/about",
            element: 
            <AuthRequire>
                <MenuAppBar/>
                <About />
            </AuthRequire>
        },
       {
            "path": "/profile",
            element:
            <AuthRequire>
            <MenuAppBar/>
                <Profile />            
            </AuthRequire>
        },
        {
            "path": "/message",
            element:
            <Message message={{
                    id: 0,
                    sender: {
                        id: 0,
                        username: 'Alice',
                        email: ''
                    },
                    channel: {
                        id: 0,
                        name: '',
                        creator: {
                            id: 0,
                            username: 'Bob',
                            email: ''
                        },
                        visibility: Visibility.PUBLIC
                    },
                    content: 'Banana very very very very very llllllllllllllllllllllllllllllllllllllllllll oooooooooooooooooooo nnnnnnnnnnnnnnnnnn    nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn   gggggggggggggg mmmmmmmmmmmmmrrrrrrrrrrrrreeeeeeeeeeeessssssssssssssssssaaaaaaaaaaaaaaggggggggggggggggggggggggeeeeeeeeeee',
                    timestamp: new Date()
                }} />
        },
        {
            "path": "/channel",
            element:
            <ChatBox />
            },
]

)

const message = [
    {
        id: 0,
        sender: {
            id: 0,
            username: 'Alice',
            email: ''
        },
        channel: {
            id: 0,
            name: '',
            creator: {
                id: 0,
                username: 'Bob',
                email: ''
            },
            visibility: Visibility.PUBLIC
        },
        content: 'Banana very very very very very llllllllllllllllllllllllllllllllllllllllllll oooooooooooooooooooo nnnnnnnnnnnnnnnnnn    nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn   gggggggggggggg mmmmmmmmmmmmmrrrrrrrrrrrrreeeeeeeeeeeessssssssssssssssssaaaaaaaaaaaaaaggggggggggggggggggggggggeeeeeeeeeee',
        timestamp: new Date()
    },
    {
        id: 1,
        sender: {
            id: 0,
            username: 'Bob',
            email: ''
        },
        channel: {
            id: 0,
            name: '',
            creator: {
                id: 0,
                username: 'Bob',
                email: ''
            },
            visibility: Visibility.PUBLIC
        },
        content: 'Banana very very very very very',
        timestamp: new Date()
    },
]

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
            <RouterProvider router={router} future={{ v7_startTransition: true }}/>
    );
  }