import { AuthenticatedUser } from '../../domain/AuthenticatedUser';
import { User } from '../../domain/User';
import { UserService } from '../interfaces/UserService';

export class UserServiceHttp implements UserService {

    private baseUrl = 'http://localhost:8080/api/user';

    async login(username: string, password: string): Promise<AuthenticatedUser> {
        try {
            const response = await fetch(`${this.baseUrl}/login`, {
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
        const response = await fetch(`${this.baseUrl}/pdm/register`, {
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

    async logOut(token: string): Promise<void> {

        const response = await fetch(`${this.baseUrl}/logout`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
        });
        if (response.ok) {
            return;
        } else {
            throw new Error('Logout failed');
        }
    }

    async updateUsername(token: string, newUsername: string): Promise<User> {
        const response = await fetch(`${this.baseUrl}/edit/username`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ newUsername: newUsername.trim() }),
        });
        if (!response.ok) {
            throw new Error('Update username failed');
        }
        const user = await response.json();
        return new User(user.id, user.username, user.email);
    }

    async getUserById(token: string, userId: number): Promise<User> {
        const response = await fetch(`${this.baseUrl}/${userId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Get user by ID failed');
        }
        const user = await response.json();
        return new User(user.id, user.username, user.email);
    }

    async searchByUsername(token: string, username: string, limit = 10, skip = 0): Promise<User[]> {
        const response = await fetch(`${this.baseUrl}/search/${username}?limit=${limit}&skip=${skip}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
        });
        if (!response.ok) {
            throw new Error('Search by username failed');
        }
        const result = await response.json();
        return result.users.map((u: any) => new User(u.id, u.username, u.email));
    }
}