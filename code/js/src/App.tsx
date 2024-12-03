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
import {ChannelDetails} from "./components/channels/channelDetails/channelDetails";
import {CreateChannel} from "./components/channels/createChannel";
import {Channel} from "./components/channel/channel";
import {Profile} from "./components/profile/profile";
import { useNavigate, useParams } from 'react-router-dom';
import {InvitationOptions} from "./components/invitation/invitationOptions";
import {ChannelInvitation} from "./components/invitation/channelInvitation";
import {RegisterInvitation} from "./components/invitation/registerInvitation";
import {InvitationRepo} from "./service/mock/repo/InvitationRepo";
import {InvitationServiceMock} from "./service/mock/InvitationServiceMock";
import { UserServiceHttp } from './service/http/UserServiceHttp'


//TODO
export function ChannelDetailsWrapper() {
    const { channelId } = useParams();
    const navigate = useNavigate();
    const repo = new ChannelRepo();
    const channel = repo.getChannelById(Number(channelId));
    if (!channel) {
        navigate('/channels');
        return null;
    }
    return (
        <ChannelDetails
            channel={channel}
            onLeave={() => console.log('Channel left')}
            loadChannels={() => console.log('Channels loaded')}
            handleLeaveChannel={(channelId: number) => console.log('Channel left')}
        />
    );
}

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
                    <ChannelDetailsWrapper />
                </AuthRequire>
            ),
        },
        {
            path: "/createChannel",
            element: 
                <AuthRequire>
                    <MenuAppBar />
                    <CreateChannel />
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
            path: "/channels/:channelId/leave/:userId",
            element: (
                <AuthRequire>
                    <MenuAppBar />
                    <ChannelsList />
                </AuthRequire>
            ),
        },
        {
            path: "/invitation",
            element: (
                <AuthRequire>
                    <MenuAppBar />
                    <InvitationOptions />
                </AuthRequire>
            ),
        },
        {
            "path": "invitation/channel",
            "element": (
                <AuthRequire>
                    <MenuAppBar />
                    <ChannelInvitation />
                </AuthRequire>
            )
        },
        {
            "path": "invitation/register",
            "element": (
                <AuthRequire>
                    <MenuAppBar />
                    <RegisterInvitation />
                </AuthRequire>
            )
        }
    ]
);

export const userRepoMock = new UserRepo()
export const channelRepoMock = new ChannelRepo()
export const messageRepoMock = new MessageRepo()
export const invitationRepoMock = new InvitationRepo()

export type Repo = {
    userRepo: UserRepo
    channelRepo: ChannelRepo
    messageRepo: MessageRepo
    invitationRepo: InvitationRepo;
}

export const repo: Repo = {
    userRepo: userRepoMock,
    channelRepo: channelRepoMock,
    messageRepo: messageRepoMock,
    invitationRepo: invitationRepoMock
}

export const mockService: Service = {
    userService: new UserServiceHttp(),
    //userService: new UserServiceMock(repo),
    channelService: new ChannelServiceMock(repo),
    messageService: new MessageServiceMock(repo),
    invitationService: new InvitationServiceMock(repo)
}

export const services: Service = mockService;

export function App() {
    return (
            <RouterProvider router={router} future={{ v7_startTransition: true }}/>
    );
  }