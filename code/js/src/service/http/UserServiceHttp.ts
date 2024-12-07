import { AuthenticatedUser } from '../../domain/AuthenticatedUser';
import { User } from '../../domain/User';
import { UserService } from '../interfaces/UserService';
import { handleResponse } from './responseHandler';

export class UserServiceHttp implements UserService {

    private baseUrl = 'http://localhost:8080/api/user';

    async login(username: string, password: string): Promise<User> {
        const response = await fetch(`${this.baseUrl}/login`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username,
                password: password }),
            credentials: "include", // Garante que cookies serão enviados e recebidos
     
        });
        const json = await handleResponse(response);
        const user: User = new User(
            json.id,
            json.username,
            json.email
        )
        return user;
    }
    async register(email: string, username: string, password: string, code:string): Promise<User> {
        const response = await fetch(`${this.baseUrl}/register/${code}`, {
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

    async logOut(): Promise<void> {
        const response = await fetch(`${this.baseUrl}/logout`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        });
        await handleResponse(response);
    }

    async updateUsername(newUsername: string): Promise<User> {
        const response = await fetch(`${this.baseUrl}/edit/username`, {
            method: 'PUT',
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({
                newUsername: newUsername.trim()
            })
        })
        const json = await handleResponse(response);
        return new User(json.id, json.username, json.email);
    }

    async getUserById(userId: number): Promise<User> {
        const response = await fetch(`${this.baseUrl}/${userId}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        })
        const json = await handleResponse(response);
        return new User(json.id, json.username, json.email);
    }

    async searchByUsername(username: string, limit: number = 10, skip: number = 0): Promise<User[]> {
        const response = await fetch(`${this.baseUrl}/search/${username}?limit=${limit}&skip=${skip}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json, application/problem+json',
                'Content-Type': 'application/json',
            },
            credentials: 'include',
        })
        const json = await handleResponse(response);
        return json.map((user: any) => new User(user.id, user.username, user.email));
    }

    async registerFirstUser(email: string, username: string, password: string): Promise<User> {
        const response = await fetch(`${this.baseUrl}/register`, {
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
}