import { User } from './User';

export type AuthenticatedUser = {
    user: User,
    token: string,
}
