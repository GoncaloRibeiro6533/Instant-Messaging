import { AuthenticatedUser } from '../../domain/AuthenticatedUser';
import { User } from '../../domain/User';
import { UserService } from '../interfaces/UserService';
import { handleResponse } from './responseHandler';

export class UserServiceHttp implements UserService {
    logOut(token: string): Promise<void> {
        throw new Error('Method not implemented.');
    }
    updateUsername(token: string, newUsername: string): Promise<User> {
        throw new Error('Method not implemented.');
    }
    getUserById(token: string, userId: number): Promise<User> {
        throw new Error('Method not implemented.');
    }
    searchByUsername(token: string, username: string): Promise<User[]> {
        throw new Error('Method not implemented.');
    }

    async login(username: string, password: string): Promise<AuthenticatedUser> {
        try {
            const response = await fetch('http://localhost:8080/api/user/login', {
                method: 'POST',
                headers: {
                    'Accept': 'application/json,  application/problem+json',
                    'Content-Type': 'application/json',
                },
                credentials: 'include'
                ,
                body: JSON.stringify({
                    username: username,
                    password: password }),
            });
            const json = await response.json(); // Usando await para aguardar o JSON
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
        } catch (error) {

        }

    }
    async register(email: string, username: string, password: string): Promise<User> {
        const response = await fetch('http://localhost:8080/api/user/register/46', {
            method: 'POST',
            headers: {
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