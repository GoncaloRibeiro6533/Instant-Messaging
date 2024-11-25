import { User } from "./User";
import { Visibility } from "./Visibility";


export type Channel = {
    id: number,
    name: string,
    creator: User,
    visibility: Visibility,
}

