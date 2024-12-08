import { User } from "../../domain/User";

export interface UserService {

    login(username: string, password: string): Promise<User>;
    register(email: string, username: string, password: string, code: string): Promise<User>;
    registerFirstUser(email: string, username: string, password: string): Promise<User>;
    logOut(): Promise<void>;
    updateUsername(newUsername: string): Promise<User>;
    getUserById(userId: number): Promise<User>;
    searchByUsername(username: string, limit?: number, skip?: number): Promise<User[]>;
}