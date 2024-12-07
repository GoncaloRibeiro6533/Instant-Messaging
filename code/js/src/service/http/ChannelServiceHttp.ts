import {ChannelService} from "../interfaces/ChannelService";
import {Channel} from "../../domain/Channel";
import {json} from "react-router-dom";
import {ChannelMember} from "../../domain/ChannelMember";
import {Role} from "../../domain/Role";
import { handleResponse} from "./responseHandler";
import { User } from "../../domain/User";
import { channelMembersMapper, channelsListMapper, channelsMapper } from "./mappers";
import { Visibility } from "../../domain/Visibility";


export class ChannelServiceHttp implements ChannelService {
    private baseUrl = 'http://localhost:8080/api/channels';

    async createChannel(channelName: string, visibility: Visibility): Promise<Channel> {
        const response = await fetch(`${this.baseUrl}`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json,  application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(
                {
                    name: channelName,
                    visibility: visibility
                }),
        });
        const json = await handleResponse(response);
        return new Channel(json.id, json.name, new User(json.creator.id, json.creator.username, json.creator.email), json.visibility);
    }

    async getChannelById(channelId: number): Promise<Channel> {
        const response = await fetch(`${this.baseUrl}/${channelId}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json,  application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        });
        const json = await handleResponse(response);
        return new Channel(json.id, json.name, new User(json.creator.id, json.creator.username, json.creator.email), json.visibility);
    }


    async getChannelMembers(channelId: number): Promise<ChannelMember[]> {
        const response = await fetch(`${this.baseUrl}/`+ channelId + '/members', {
            method: 'GET',
            headers: {
                'Accept': 'application/json,  application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        });
        const json = await handleResponse(response);
        return channelMembersMapper(json);
    }

    async getChannelsOfUser(userId: number): Promise<Map<Channel, Role>> {
        const response = await fetch(`${this.baseUrl}/user/`+ userId, {
            method: 'GET',
            headers: {
                'Accept': 'application/json,  application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        });
        const json = await handleResponse(response);
        return channelsListMapper(json);

    }

    async joinChannel(channelId: number, role: Role): Promise<Channel> {
        const response = await fetch(`${this.baseUrl}/${channelId}/add/${role}`, {
            method: 'PUT',
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        });
        const json = await handleResponse(response);
        return new Channel(json.id, json.name, new User(json.creator.id, json.creator.username, json.creator.email), json.visibility);
    }

    async leaveChannel(channelId: number): Promise<Channel> {
        const response = await fetch(`${this.baseUrl}/${channelId}/leave`, {
            method: 'PUT',
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        })
        const json = await handleResponse(response);
        return new Channel(json.id, json.name, new User(json.creator.id, json.creator.username, json.creator.email), json.visibility);
    }

    async searchChannelByName(name: string, limit: number = 10, skip: number = 0): Promise<Channel[]> {
        const response = await fetch(`${this.baseUrl}/search/${name}?limit=${limit}&skip=${skip}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        });
        const json = await handleResponse(response);
        return channelsMapper(json);
        }

    async updateChannelName(channelId: number, newName: string): Promise<Channel> {
        const response = await fetch(`${this.baseUrl}/${channelId}/${newName}`, {
            method: 'PUT',
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        });
        const json = await handleResponse(response);
        return new Channel(json.id, json.name, new User(json.creator.id, json.creator.username, json.creator.email), json.visibility);
    }
}
