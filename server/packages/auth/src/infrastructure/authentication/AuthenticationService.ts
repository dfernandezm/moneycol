import FirebaseAuthenticationService from "./firebase/FirebaseAuthenticationService";
import { firebaseInstance } from "@moneycol-server/users";
import { userRepository } from '@moneycol-server/users';
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
    provider: string,
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

export type GoogleAuthMaterial = {
    idToken: string,
    // tokenExpiresAt: number,
    // tokenExpiresIn: number,
    // googleId: string,
    // email: string,
    // name: string,
    // imageUrl: string
}

export interface AuthenticationService {
      loginWithEmailPassword(email: string, password: string): Promise<AuthenticationResult>;
      logout(token: string): Promise<object>;
      validateToken(token: string, refresh: boolean): Promise<AuthenticationResult>;
      changePassword(changePasswordCommand: ChangePasswordCommand): Promise<ChangePasswordResult>;
      resetPasswordRequest(email: string): Promise<object>;
      completeResetPassword(completeResetPasswordCmd: CompleteResetPasswordCommand): Promise<object>;
      loginWithGoogle(googleAuthMaterial: GoogleAuthMaterial): Promise<AuthenticationResult>;
}

export const authenticationService = new FirebaseAuthenticationService(firebaseInstance, userRepository, userSessionRepository);