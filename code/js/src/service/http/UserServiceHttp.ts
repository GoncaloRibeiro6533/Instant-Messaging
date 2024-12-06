import { AuthenticatedUser } from '../../domain/AuthenticatedUser';
import { User } from '../../domain/User';
import { UserService } from '../interfaces/UserService';
import { handleResponse } from './responseHandler';

export class UserServiceHttp implements UserService {

    private baseUrl = 'http://localhost:8080/api/user';

    async login(username: string, password: string): Promise<AuthenticatedUser> {
        const response = await fetch(`${this.baseUrl}/login`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username,
                password: password }),
        });
        const json = await handleResponse(response);
        const user: User = new User(
            json.user.id,
            json.user.username,
            json.user.email
        )
        const authUser: AuthenticatedUser = {
            token: json.token,
            user: user
        };
        return authUser;
    }
    async register(email: string, username: string, password: string, invitationID:number): Promise<User> {
        const response = await fetch(`${this.baseUrl}/register/${invitationID}`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: email,
                username: username,
                password: password
            }),
        });
        const json = await handleResponse(response);
        return new User(json.id, json.username, json.email);
    }

    async logOut(token: string): Promise<void> {
        const response = await fetch(`${this.baseUrl}/logout`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
        });
        await handleResponse(response);
    }

    async updateUsername(token: string, newUsername: string): Promise<User> {
        const response = await fetch(`${this.baseUrl}/edit/username`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                newUsername: newUsername.trim()
            })
        })
        const json = await handleResponse(response);
        return new User(json.id, json.username, json.email);
    }

    async getUserById(token: string, userId: number): Promise<User> {
        const response = await fetch(`${this.baseUrl}/${userId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            }
        })
        const json = await handleResponse(response);
        return new User(json.id, json.username, json.email);
    }

    async searchByUsername(token: string, username: string, limit: number = 10, skip: number = 0): Promise<User[]> {
        const response = await fetch(`${this.baseUrl}/search/${username}?limit=${limit}&skip=${skip}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            }
        })
        const json = await handleResponse(response);
        return json.map((user: any) => new User(user.id, user.username, user.email));
    }
}