import {User} from "../../domain/User";
import {UserService} from "../interfaces/UserService";
import {delay} from "./utils";
import {Repo} from "../../App";
import { tokenHandler } from "./tokenHandler";

export class UserServiceMock implements UserService {
    repo: Repo;

    constructor(repo: Repo) {
        this.repo = repo;
    }
    
    async login(username: string, password: string): Promise<User | undefined> {
        await delay(500);
        const user = this.repo.userRepo.users.find(user => user.username === username);
        if (!user) {
            return undefined;
        }
        const storedPassword = this.repo.userRepo.usersPassword.get(user.id);
        if (storedPassword !== password) {
            return undefined;
        }
        const token = this.repo.userRepo.createToken(user.id);
        tokenHandler().setToken(token);
        console.log(this.repo.userRepo.tokens);
        return Promise.resolve(user)
    }

    async register(email: string, username:string, password: string, code:string): Promise<User> {
        await delay(1000);
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
        //TODO check if email is already in use
        if (this.repo.userRepo.users.find(user => user.email === email)) {
            throw new Error("Email already in use");
        }
        if (this.repo.userRepo.users.find(user => user.username === username)) {
            throw new Error("Username already in use");
        }
        //TODO check if invitationId is valid
        return this.repo.userRepo.createUser(username, email, password);
    }

    async logOut(): Promise<void> {
        await delay(1000);
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
        this.repo.userRepo.deleteToken(token);
    }

    async updateUsername(newUsername: string): Promise<User> {
        await delay(1000);
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
        const user = this.repo.userRepo.getUserByToken(token);
        if (!user) {
            throw new Error("User not found");
        }
        if (this.repo.userRepo.users.find(user => user.username === newUsername)) {
            throw new Error("Username already in use");
        }
        user.username = newUsername;
        this.repo.userRepo.updateUser(user);
        return user;
    }

    async getUserById(userId: number): Promise<User> {
        await delay(500);
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
        const userVerify = this.repo.userRepo.getUserByToken(token);
        if (!userVerify) {
            throw new Error("Invalid token");
        }
        const user = this.repo.userRepo.users.find(user => user.id === userId);
        if (!user) {
            throw new Error("User not found");
        }
        return user;
    }

    async searchByUsername(username: string, limit: number = 10, skip: number = 0): Promise<User[]> {
        await delay(500);
        const token = tokenHandler().getToken();
        if(!token) throw new Error("Invalid token");
        if(!this.repo.userRepo.getUserByToken(token)) throw new Error("Invalid token");
        const userVerify = this.repo.userRepo.getUserByToken(token);
        if (!userVerify) {
            throw new Error("Invalid token");
        }
        return new Promise<User[]>((resolve) => {
            const users = this.repo.userRepo.getUserByUsername(username);
            resolve(Array.isArray(users) ? users : [users].filter(Boolean));
        });
    }

    async registerFirstUser(email: string, username: string, password: string): Promise<User> {
        await delay(1000);
        if (this.repo.userRepo.users.length > 0) {
            throw new Error("Users already exist");
        }
        return this.repo.userRepo.createUser(username, email, password);
    }
}

