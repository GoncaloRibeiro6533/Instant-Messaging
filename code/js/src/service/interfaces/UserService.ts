import { AuthenticatedUser } from "../../domain/AuthenticatedUser";
import { User } from "../../domain/User";

export interface UserService {

    login(username: string, password: string): Promise<AuthenticatedUser>;
    register(email: string, username: string, password: string, invitationId: number): Promise<User>;
    logOut(token: string): Promise<void>;
    updateUsername(token: string, newUsername: string): Promise<User>;
    getUserById(token: string, userId: number): Promise<User>;
}