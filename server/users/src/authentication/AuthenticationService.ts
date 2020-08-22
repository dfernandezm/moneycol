import FirebaseAuthenticationService from "./firebase/FirebaseAuthenticationService";
import { firebaseInstance } from './firebase/FirebaseConfiguration';
import { userRepository } from '../users/UserService';
import { userSessionRepository } from './firebase/UserSessionRepository';

export type AuthenticationResult = {
    userId: string;
    email: string | null;
    token: string;
}

export type User = {
    token: string,
    refreshToken: string,
    email: string | null,
    userId: string,
    lastLogin: Date
}

export type ChangePasswordCommand = {
    email: string,
    oldPassword: string,
    newPassword: string,
    newPasswordRepeated: string
}

export type ChangePasswordResult = {
    result: string;
}

export type CompleteResetPasswordCommand = {
    email: string,
    resetCode: string,
    newPassword: string
}

export interface AuthenticationService {
      loginWithEmailPassword(email: string, password: string): Promise<AuthenticationResult>;
      logout(token: string): Promise<object>;
      validateToken(token: string, refresh: boolean): Promise<AuthenticationResult>;
      changePassword(changePasswordCommand: ChangePasswordCommand): Promise<ChangePasswordResult>;
      resetPasswordRequest(email: string): Promise<object>;
      completeResetPassword(completeResetPasswordCmd: CompleteResetPasswordCommand): Promise<object>;

}

export const authenticationService = new FirebaseAuthenticationService(firebaseInstance, userRepository, userSessionRepository);