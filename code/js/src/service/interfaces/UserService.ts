import { AuthenticatedUser } from "../../domain/AuthenticatedUser";
import { User } from "../../domain/User";

export interface UserService {

    login(username: string, password: string): Promise<AuthenticatedUser>;
    register(username: string, email: string, password: string): Promise<AuthenticatedUser>;
    logOut(token: string): Promise<void>;
    updateUsername(token: string, newUsername: string): Promise<User>;
    getUserById(token: string, userId: number): Promise<User>;
}