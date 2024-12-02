import { AuthenticatedUser } from '../../domain/AuthenticatedUser';

/*
export class  UserServiceHttp {

    async login(username: string, password: string): Promise<AuthenticatedUser> {
        try {
        const response = await fetch('http://localhost:8080/api/user/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ 
                username: username, 
                password: password }),
        });
    } catch (error) {

    }
        
    }
    async register(username: string, password: string): Promise<AuthenticatedUser> {
        const response = await fetch('http://localhost:8080/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }),
        });
        if (response.status === 200) {
            const user = await response.json();
            return user;
        } else {
            throw new Error('Registration failed');
        }
    }
}*/