import { User } from "./User";
import { Visibility } from "./Visibility";

/*
export type Channel = {
    id: number,
    name: string,
    creator: User,
    visibility: Visibility,
}*/

export class Channel {
    id: number;
    name: string;
    creator: User;
    visibility: Visibility;

    constructor(id: number, name: string, creator: User, visibility: Visibility) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.visibility = visibility;
    }

    public equals(other: Channel): boolean {
        return this.id === other.id && this.name === other.name && this.creator.equals(other.creator) && this.visibility === other.visibility;
    }
}