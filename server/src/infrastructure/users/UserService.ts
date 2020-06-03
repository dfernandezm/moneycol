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

export type EmailVerificationCommand = {
    email: string,
    actionCode: string,
    continueUrl: string,
    lang: string
}

//TODO: should be the same as the command
export type VerifyEmailInput = {
    email: string,
    code: string,
    comebackUrl: string,
    lang: string
}

export type EmailVerificationResult = {
    email: string,
    result: string,
    comebackUrl?: string,
}

export interface UserRepository {
    persistUser(user: User): Promise<any>;
    updateUser(email: string): Promise<any>;
   // byEmail(email: string): Promise<User>;
   // updateUserTo(userId: string, userData: User): Promise<User>;
}

export interface UserService {
    signUpWithEmail(createUserCommand: CreateUserCommand): Promise<UserCreatedResult>;
    verifyUserEmail(emailVerificationCommand: EmailVerificationCommand): Promise<EmailVerificationResult>;
}

export interface EmailService {
    verifyEmail(actionCode: string, continueUrl: string, lang: string): Promise<object>;
    generateComebackUrl(email: string) : string;

}

export const userRepository = new FirestoreUserRepository();
