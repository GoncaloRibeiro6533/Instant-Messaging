import {createBrowserRouter, RouterProvider,} from 'react-router-dom'
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
import {ChannelsList} from "./components/channels/channelsList/channelsList";
import {ChannelDetails} from "./components/channels/channelDetails";
import {CreateChannel} from "./components/channels/createChannel";

const router = createBrowserRouter(
    [
        {
            "path": "/",
            element:
                <><MenuAppBar /><Home /></>,
            "children": []
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
                <>
                    <MenuAppBar/>
                    <About />
                </>
        },
        {
            "path": "/channels",
            element:
                <AuthRequire>
                    <MenuAppBar/>
                    <ChannelsList/>
                </AuthRequire>
        },
        /*{
            "path": "/profile",
            element:
            <AuthRequire>
                <MenuAppBar/>
                <Profile />
            </AuthRequire>
        }

         */
        {
            "path": "/channels/:id",
            element:
                <AuthRequire>
                    <MenuAppBar/>
                    <ChannelDetails/>
                </AuthRequire>
        },
        {
            "path": "/createChannel",
            element:
                <AuthRequire>
                    <MenuAppBar/>
                    <CreateChannel />
                </AuthRequire>
        }
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
        <RouterProvider router={router} future={{ v7_startTransition: true }}/>
    );
}