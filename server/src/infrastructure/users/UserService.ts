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
    actionCode: string,
    continueUrl: string,
    lang: string
}

//TODO: should be the same as the command
export type VerifyEmailInput = {
    code: string,
    comebackUrl: string,
    lang: string
}

export type EmailVerificationResult = {
    result: string,
    email: string,
    comebackUrl?: string,
}

export interface UserRepository {
    persistUser(user: User): Promise<any>;
    byEmail(email: string): Promise<User>;
    updateUserData(userData: User): Promise<object>;
}

export interface UserService {
    signUpWithEmail(createUserCommand: CreateUserCommand): Promise<UserCreatedResult>;
    verifyUserEmail(emailVerificationCommand: EmailVerificationCommand): Promise<EmailVerificationResult>;
}

export interface EmailService {
    verifyEmail(actionCode: string, continueUrl: string, lang: string): Promise<EmailVerificationResult>;
    generateComebackUrl(email: string) : string;

}

export const userRepository = new FirestoreUserRepository();
