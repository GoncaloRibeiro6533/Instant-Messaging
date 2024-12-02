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

    public static fromRaw(raw: any): Channel {
        return new Channel(
            raw.id,
            raw.name,
            User.fromRaw(raw.creator), // Criar User a partir do raw
            raw.visibility as Visibility // Cast para Visibility
        );
    }

    // Método para serializar um Channel para um objeto simples
    public toRaw(): any {
        return {
            id: this.id,
            name: this.name,
            creator: this.creator.toRaw(), // Serializa User
            visibility: this.visibility,
        };
    }

}