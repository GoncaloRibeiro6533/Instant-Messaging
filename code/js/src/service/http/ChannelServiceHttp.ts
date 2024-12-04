import {ChannelService} from "../interfaces/ChannelService";
import {Channel} from "../../domain/Channel";
import {json} from "react-router-dom";
import {ChannelMember} from "../../domain/ChannelMember";
import {Role} from "../../domain/Role";

export class ChannelServiceHttp implements ChannelService {
    // @ts-ignore todo
    async createChannel(token: string, channelName: string): Promise<Channel> {
        const response = await fetch('http://localhost:8080/api/channel', {
            method: 'POST',
            headers: {
                'Accept': 'application/json,  application/problem+json',
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({name: channelName}),
        });
        const json = await response.json();
        //return new Channel(json.id, json.name);
    }

    // @ts-ignore todo
    async getChannelById(token: string, channelId: number): Promise<Channel> {
        const response = await fetch('http://localhost:8080/api/channel/' + channelId, {
            method: 'GET',
            headers: {
                'Accept': 'application/json,  application/problem+json',
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
        });
        const json = await response.json();
        //return new Channel(json.id, json.name);
    }


    getChannelMembers(token: string, channelId: number): Promise<ChannelMember[]> {
        return Promise.resolve([]);
    }

    getChannelsOfUser(token: string, userId: number): Promise<Map<Channel, Role>> {
        return Promise.resolve(undefined);
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