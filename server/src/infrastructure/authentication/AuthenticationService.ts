import FirebaseAuthenticationService from "./firebase/FirebaseAuthenticationService";

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

export interface AuthenticationService {
      loginWithEmailPassword(email: string, password: string): Promise<AuthenticationResult>;
      logout(token: string): Promise<object>;
      validateToken(token: string, refresh: boolean): Promise<AuthenticationResult>;
}

export const authenticationService = new FirebaseAuthenticationService();