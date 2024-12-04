import { AuthenticatedUser } from '../../domain/AuthenticatedUser';
import { User } from '../../domain/User';
import { UserService } from '../interfaces/UserService';

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
    async register(username: string, password: string): Promise<User> {
        const response = await fetch('http://localhost:8080/api/user/pdm/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }),
        });
        if (response.status === 200) {
            const user = await response.json();
            return new User(user.id, user.username, user.email);
        } else {
            throw new Error('Registration failed');
        }
    }
}