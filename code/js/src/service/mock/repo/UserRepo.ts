import { User } from '../../../domain/User';

export interface UserRepoInterface {
    users: Array<User>;
    usersPassword: Map<number, string>;
    tokens: Map<string, number>;
    nextId: number;

    createUser(username: string, email: string, password: string): User;
    getUserById(id: number): User | undefined;
    getUserByUsername(username: string): User | undefined;
    getUserByEmail(email: string): User | undefined;
    getUserByToken(token: string): User | undefined;
    updateUser(user: User): void;
    deleteUser(id: number): void;

   
}

export class UserRepo implements UserRepoInterface {
    public nextId = 2;
    public tokens: Map<string, number> = new Map();
    public users: Array<User> = [
        {
            id: 1,
            username: 'Bob',
            email: 'bob@example.com'
        }
    ];
    public usersPassword: Map<number, string> = new Map([
        [1, '123'],
    ]);

    createUser(username: string, email: string, password: string): User {
        const user = {
            id: this.nextId++,
            username,
            email
        };
        this.users.push(user);
        this.usersPassword.set(user.id, password);
        return user;
    }

    getUserById(id: number): User | undefined {
        return this.users.find(user => user.id === id);
    }

    getUserByUsername(username: string): User | undefined {
        return this.users.find(user => user.username === username);
    }

    getUserByEmail(email: string): User | undefined {
        return this.users.find(user => user.email === email);
    }

    getUserByToken(token: string): User | undefined {
        const userId = this.tokens.get(token);
        if (!userId) {
            return undefined;
        }
        return this.getUserById(userId);
    }

    updateUser(user: User): void {
        const index = this.users.findIndex(u => u.id === user.id);
        this.users[index] = user;
    }

    deleteUser(id: number): void {
        this.users = this.users.filter(user => user.id !== id);
    }
}

export let userRepo = new UserRepo();