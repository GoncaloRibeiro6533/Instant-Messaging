import { AuthenticatedUser } from "../../domain/AuthenticatedUser";
import { User } from "../../domain/User";
import { UserService } from "../interfaces/UserService";
import { delay } from "./utils";
import { Repo } from "../../App";

export class UserServiceMock implements UserService {
    repo: Repo;

    constructor(repo: Repo) {
        this.repo = repo;
    }

    async login(username: string, password: string): Promise<AuthenticatedUser | undefined> {
        await delay(1000);
        const user = this.repo.userRepo.users.find(user => user.username === username);
        if (!user) {
            return undefined;
        }
        const storedPassword = this.repo.userRepo.usersPassword.get(user.id);
        if (storedPassword !== password) {
            return undefined;
        }
        const token = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
        this.repo.userRepo.tokens.set(token, user.id);
        return Promise.resolve({
            user,
            token
        })
    }

    async register(email: string, password: string): Promise<AuthenticatedUser> {
        await delay(1000);
        const user = {
            id: this.repo.userRepo.nextId++,
            username: email.split('@')[0],
            email
        };
        this.repo.userRepo.users.push(user);
        this.repo.userRepo.usersPassword.set(user.id, password);
        const token = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
        this.repo.userRepo.tokens.set(token, user.id);
        return {
            user,
            token
        };
    }

    async logOut(token: string): Promise<void> {
        await delay(1000);
        this.repo.userRepo.tokens.delete(token);
    }

    async updateUsername(token: string, newUsername: string): Promise<User> {
        await delay(1000);
        const userId = this.repo.userRepo.tokens.get(token);
        if (!userId) {
            throw new Error("Invalid token");
        }
        const user = this.repo.userRepo.users.find(user => user.id === userId);
        if (!user) {
            throw new Error("User not found");
        }
        user.username = newUsername;
        return user;
    }

    async getUserById(token: string, userId: number): Promise<User> {
        await delay(500);
        const userVerify = this.repo.userRepo.tokens.get(token);
        if (!userVerify) {
            throw new Error("Invalid token");
        }
        const user = this.repo.userRepo.users.find(user => user.id === userId);
        if (!user) {
            throw new Error("User not found");
        }
        return user;
    }

}

