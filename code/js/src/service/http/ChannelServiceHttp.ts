import {ChannelService} from "../interfaces/ChannelService";
import {Channel} from "../../domain/Channel";
import {json} from "react-router-dom";
import {ChannelMember} from "../../domain/ChannelMember";
import {Role} from "../../domain/Role";
import { handleResponse} from "./responseHandler";
import { User } from "../../domain/User";
import { channelMembersMapper, channelsListMapper } from "./mappers";
import { Visibility } from "../../domain/Visibility";


export class ChannelServiceHttp implements ChannelService {
    async createChannel(token: string, channelName: string, visibility: Visibility): Promise<Channel> {
        const response = await fetch('http://localhost:8080/api/channels', {
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

    // @ts-ignore todo
    async getChannelById(token: string, channelId: number): Promise<Channel> {
        const response = await fetch('http://localhost:8080/api/channels/' + channelId, {
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
        const response = await fetch('http://localhost:8080/api/channels/'+ channelId + '/members', {
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
        const response = await fetch('http://localhost:8080/api/channels/user/'+ userId, {
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

    joinChannel(token: string, channelId: number, role: Role): Promise<Channel> {
        return Promise.resolve(undefined);
    }

    leaveChannel(token: string, channelId: number): Promise<Channel> {
        return Promise.resolve(undefined);
    }

    searchChannelByName(token: string, name: string, limit: number, skip: number): Promise<Channel[]> {
        return Promise.resolve([]);
    }

    updateChannelName(token: string, channelId: number, newName: string): Promise<Channel> {
        return Promise.resolve(undefined);
    }
}