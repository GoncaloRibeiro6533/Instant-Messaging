import {createBrowserRouter, RouterProvider} from 'react-router-dom'
import { Home } from './components/home/home'
import { Login } from './components/login/login'
import * as React from 'react'
import Service from './service/Service'
import { UserRepo } from './service/mock/repo/UserRepo'
import { ChannelRepo } from './service/mock/repo/ChannelRepo'
import { MessageRepo } from './service/mock/repo/MessageRepo'
import { About } from './components/about/about'
import MenuAppBar from './components/navBar/navigationBar'
import { Register } from './components/register/register'
import { AuthRequire } from './components/auth/AuthRequire'
import {ChannelsList} from "./components/channels/channelsList/channelsList"
import {ChannelDetails} from "./components/channels/channelDetails/channelDetails"
import {CreateChannel} from "./components/channels/createChannel"
import {Channel} from "./components/channel/channel"
import {Profile} from "./components/profile/profile"
import {InvitationOptions} from "./components/invitation/invitationOptions"
import {ChannelInvitation} from "./components/invitation/channelInvitation"
import {RegisterInvitation} from "./components/invitation/registerInvitation"
import {InvitationRepo} from "./service/mock/repo/InvitationRepo"
import {UserServiceHttp} from "./service/http/UserServiceHttp"
import {ChannelServiceHttp} from "./service/http/ChannelServiceHttp"
import {MessageServiceHttp} from "./service/http/MessageServiceHttp"
import { InvitationServiceHttp } from './service/http/InvitationServiceHttp'
import { RegisterFirstUser } from './components/registerFirstUser/registerFirstUser'

import {InvitationsList} from "./components/invitation/invitationsList";
import {UserServiceMock} from "./service/mock/UserServiceMock";
import {ChannelServiceMock} from "./service/mock/ChannelServiceMock";
import {MessageServiceMock} from "./service/mock/MessageServiceMock";
import {InvitationServiceMock} from "./service/mock/InvitationServiceMock";
import {Notification} from "./components/notifications/notification";


const router = createBrowserRouter(
    [
        {
            "path": "/",
            element:
                <>
                    <MenuAppBar /> 
                    <Notification/> 
                    <Home />
                </>,
            "children": []
        },
        {
            "path": "/register/:code",
            element: <Register/>
        },
        {
            "path": "/register",
            element: <RegisterFirstUser/>
        },
        {
            "path": "/login",
            element:<Login />
        },
        {
            "path": "/about",
            element:
                <>
                    <MenuAppBar/>
                    <Notification/> 
                    <About />
                </>
        },
        {
            "path": "/channels",
            element:
                <AuthRequire>
                    <MenuAppBar/>
                    <Notification/> 
                    <ChannelsList/>
                </AuthRequire>,
            children: [
                {
                    "path": "channel/:channelId",
                    element: <Channel/>
                },
            ]
        },
        {
            path: "/channel/:channelId",
            element: (
                <AuthRequire>
                    <MenuAppBar />
                    <ChannelDetails />
                </AuthRequire>
            ),
        },
        {
            path: "/channels/create",
            element:
                <AuthRequire>
                    <MenuAppBar />
                    <Notification/> 
                    <CreateChannel />
                </AuthRequire>
        },
        {
            "path": "/profile",
            element:
                <AuthRequire>
                    <MenuAppBar/>
                    <Notification/> 
                    <Profile />
                </AuthRequire>
        },

        {
            path: "/channels/:channelId/leave/:userId",
            element: (
                <AuthRequire>
                    <MenuAppBar />
                    <Notification/> 
                    <ChannelDetails />
                </AuthRequire>
            ),
        },
        {
            "path": "/invitation/channel/:channelId",
            "element": (
                <AuthRequire>
                    <MenuAppBar />
                    <Notification/> 
                    <ChannelInvitation />
                </AuthRequire>
            )
        },
        {
            "path": "/invitation/register/:channelId",
            "element": (
                <AuthRequire>
                    <MenuAppBar />
                    <Notification/> 
                    <RegisterInvitation />
                </AuthRequire>
            )
        },
        {
            "path": "/invitations",
            "element": (
                <AuthRequire>
                    <MenuAppBar />
                    <Notification/> 
                    <InvitationsList />
                </AuthRequire>
            )
        }
    ]
)

export const userRepoMock = new UserRepo()
export const channelRepoMock = new ChannelRepo()
export const messageRepoMock = new MessageRepo()
export const invitationRepoMock = new InvitationRepo()

export type Repo = {
    userRepo: UserRepo
    channelRepo: ChannelRepo
    messageRepo: MessageRepo
    invitationRepo: InvitationRepo
}

export const repo: Repo = {
    userRepo: userRepoMock,
    channelRepo: channelRepoMock,
    messageRepo: messageRepoMock,
    invitationRepo: invitationRepoMock
}

export const mockService: Service = {
    userService: new UserServiceMock(repo),
    channelService: new ChannelServiceMock(repo),
    messageService: new MessageServiceMock(repo),
    invitationService: new InvitationServiceMock(repo)
}

export const httpService: Service = {
    userService: new UserServiceHttp(),
    channelService: new ChannelServiceHttp(),
    messageService: new MessageServiceHttp(),
    invitationService: new InvitationServiceHttp(),
}

export const useMock = true


export const services: Service = useMock ? mockService : httpService

export function App() {
    return (
            <RouterProvider router={router} future={{ v7_startTransition: true }}/>
    )
}