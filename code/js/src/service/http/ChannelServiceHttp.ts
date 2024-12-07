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

    async createChannel(token: string, channelName: string, visibility: Visibility): Promise<Channel> {
        const response = await fetch(`${this.baseUrl}`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json,  application/problem+json',
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify(
                {
                    name: channelName,
                    visibility: visibility
                }),
        });
        const json = await handleResponse(response);
        return new Channel(json.id, json.name, new User(json.creator.id, json.creator.username, json.creator.email), json.visibility);
    }

    async getChannelById(token: string, channelId: number): Promise<Channel> {
        const response = await fetch(`${this.baseUrl}/${channelId}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json,  application/problem+json',
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
        });
        const json = await handleResponse(response);
        return new Channel(json.id, json.name, new User(json.creator.id, json.creator.username, json.creator.email), json.visibility);
    }


    async getChannelMembers(token: string, channelId: number): Promise<ChannelMember[]> {
        const response = await fetch(`${this.baseUrl}/`+ channelId + '/members', {
            method: 'GET',
            headers: {
                'Accept': 'application/json,  application/problem+json',
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
        });
        const json = await handleResponse(response);
        return channelMembersMapper(json);
    }

    async getChannelsOfUser(token: string, userId: number): Promise<Map<Channel, Role>> {
        const response = await fetch(`${this.baseUrl}/user/`+ userId, {
            method: 'GET',
            headers: {
                'Accept': 'application/json,  application/problem+json',
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
        });
        const json = await handleResponse(response);
        return channelsListMapper(json);

    }

    async joinChannel(token: string, channelId: number, role: Role): Promise<Channel> {
        const response = await fetch(`${this.baseUrl}/${channelId}/add/${role}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            }
        });
        const json = await handleResponse(response);
        return new Channel(json.id, json.name, new User(json.creator.id, json.creator.username, json.creator.email), json.visibility);
    }

    async leaveChannel(token: string, channelId: number): Promise<Channel> {
        const response = await fetch(`${this.baseUrl}/${channelId}/leave`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            }
        })
        const json = await handleResponse(response);
        return new Channel(json.id, json.name, new User(json.creator.id, json.creator.username, json.creator.email), json.visibility);
    }

    async searchChannelByName(token: string, name: string, limit: number = 10, skip: number = 0): Promise<Channel[]> {
        const response = await fetch(`${this.baseUrl}/search/${name}?limit=${limit}&skip=${skip}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            }
        });
        const json = await handleResponse(response);
        return channelsMapper(json);
        }

    async updateChannelName(token: string, channelId: number, newName: string): Promise<Channel> {
        const response = await fetch(`${this.baseUrl}/${channelId}/${newName}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            }
        });
        const json = await handleResponse(response);
        return new Channel(json.id, json.name, new User(json.creator.id, json.creator.username, json.creator.email), json.visibility);
    }
}
