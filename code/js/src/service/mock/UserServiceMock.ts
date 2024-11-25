import { AuthenticatedUser } from "../../domain/AuthenticatedUser";
import { User } from "../../domain/User";
import { UserService } from "../interfaces/UserService";
import { delay } from "./utils";

export class UserServiceMock implements UserService {
   
    private nextId = 2;
    private tokens: Map<string, number> = new Map();
    private users: Array<User> = [
        {
            id: 1,
            username: 'Bob',
            email: 'bob@example.com'
        }
    ];
    private usersPassword: Map<number, string> = new Map([
        [1, '123'],
    ]);

    async login(username: string, password: string): Promise<AuthenticatedUser | undefined> {
        await delay(1000);
        const user = this.users.find(user => user.username === username);
        if (!user) {
            return undefined;
        }
        const storedPassword = this.usersPassword.get(user.id);
        if (storedPassword !== password) {
            return undefined;
        }
        const token = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
        this.tokens.set(token, user.id);
        return Promise.resolve({
            user,
            token
        })
    }

    async register(email: string, password: string): Promise<AuthenticatedUser> {
        await delay(1000);
        const user = {
            id: this.nextId++,
            username: email.split('@')[0],
            email
        };
        this.users.push(user);
        this.usersPassword.set(user.id, password);
        const token = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
        this.tokens.set(token, user.id);
        return {
            user,
            token
        };
    }

    async logOut(token: string): Promise<void> {
        await delay(1000);
        this.tokens.delete(token);
    }

    async updateUsername(token: string, newUsername: string): Promise<User> {
        await delay(1000);
        const userId = this.tokens.get(token);
        if (!userId) {
            throw new Error("Invalid token");
        }
        const user = this.users.find(user => user.id === userId);
        if (!user) {
            throw new Error("User not found");
        }
        user.username = newUsername;
        return user;
    }
}

