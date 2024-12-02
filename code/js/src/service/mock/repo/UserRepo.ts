import { User } from '../../../domain/User';

export interface UserRepoInterface {
    users: Array<User>;
    usersPassword: Map<number, string>;
    tokens:  Map<number, Array<string>>
    nextId: number;

    createUser(username: string, email: string, password: string): User;
    getUserById(id: number): User | undefined;

    getUserByUsername(username: string): User[];
    getUserByEmail(email: string): User | undefined;
    getUserByToken(token: string): User | undefined;
    updateUser(user: User): void;
    deleteUser(id: number): void;
    deleteToken(token: string): void;
    createToken(userId: number): string;
    addToken(userId: number, token: string): void;
   
}

export class UserRepo implements UserRepoInterface {
    constructor() {
        const savedUser = localStorage.getItem("user");
        const initialUser = savedUser ? JSON.parse(savedUser) : undefined;
        if (initialUser) {
            const id = initialUser.user.id;  
           this.addToken(initialUser.user.id, initialUser.token);
           // update memo with local storage user
           this.users = this.users.filter(user => user.id !== id);
            this.users.push(initialUser.user);
        }
    }
    public nextId = 2;
    public tokens: Map<number, Array<string>> = new Map(
        [[1, ["123"]]]
    );
    public users: Array<User> = [
        {
            id: 1,
            username: 'Bob',
            email: 'bob@example.com'
        },
        {
            id: 2,
            username: 'Alice',
            email: 'alice@example.com'
        }
    ];
    public usersPassword: Map<number, string> = new Map([
        [1, '123'],
    ]);

    private invitations = new Map<number, string>(
        [[1, "alice@example.com"]]
    );


    addToken(userId: number, token: string): void {
        if (this.tokens.has(userId)) {
            this.tokens.get(userId)!.push(token);
        } else {
            this.tokens.set(userId, [token]);
        }
    }
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

    getUserByUsername(username: string): User[] {
        return this.users.filter(user => user.username.includes(username));
    }

    getUserByEmail(email: string): User | undefined {
        return this.users.find(user => user.email === email);
    }

    getUserByToken(token: string): User | undefined {
        // Percorre o mapa para encontrar o usuário associado ao token
        for (const [userId, tokens] of this.tokens.entries()) {
            if (tokens.includes(token)) {
                // Retorna o usuário correspondente
                return this.getUserById(userId);
            }
        }
        // Retorna undefined se o token não for encontrado
        return undefined;
    }

    createToken(userId: number): string {
        // Gera um token aleatório
        const token = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
        // Adiciona o token ao mapa
        if (this.tokens.has(userId)) {
            this.tokens.get(userId)!.push(token);
        } else {
            this.tokens.set(userId, [token]);
        }
        // Retorna o token gerado
        return token;
    }

    updateUser(user: User): void {
        const index = this.users.findIndex(u => u.id === user.id);
        this.users[index] = user;
    }

    deleteUser(id: number): void {
        this.users = this.users.filter(user => user.id !== id);
    }

    deleteToken(token: string): void {
        // Percorre o mapa para encontrar o usuário associado ao token
        for (const [userId, tokens] of this.tokens.entries()) {
            // Remove o token do usuário
            this.tokens.set(userId, tokens.filter(t => t !== token));
        }
    }
}

export let userRepo = new UserRepo();