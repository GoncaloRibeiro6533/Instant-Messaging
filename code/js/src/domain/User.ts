
/*
export type User = {
    id: number,
    username: string,
    email: string,    
}*/

export class User {
    id: number;
    username: string;
    email: string;

    constructor(id: number, username: string, email: string) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public equals(other: User): boolean {
        return this.id === other.id && this.username === other.username && this.email === other.email;
    }


    public static fromRaw(raw: any): User {
        return new User(raw.id, raw.username, raw.email);
    }

    public toRaw(): any {
        return {
            id: this.id,
            username: this.username,
            email: this.email,
        };
    }
}