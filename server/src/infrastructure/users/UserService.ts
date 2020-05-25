import FirestoreUserRepository from "./firebase/FirestoreUserRepository";

export type CreateUserCommand = {
    username: string,
    email: string,
    password: string,
    repeatedPassword: string,
    firstName: string,
    lastName: string
    [key: string]: string
}

export type UserCreatedResult = {
    userId: string,
    username: string,
    email: string,
    firstName: string,
    lastName: string
}

export enum UserStatus {
    PENDING_VERIFICATION = "PENDING_VERIFICATION",
    ACTIVE = "ACTIVE",
    SUSPENDED = "SUSPENDED"
}

type ExtraUserProperties = {
    status: UserStatus
}

export type User = UserCreatedResult & ExtraUserProperties;

export interface UserRepository {
    persistUser(user: User): Promise<any>;
}

export interface UserService {
    signUpWithEmail(createUserCommand: CreateUserCommand): Promise<UserCreatedResult>;
}

export const userRepository = new FirestoreUserRepository();
