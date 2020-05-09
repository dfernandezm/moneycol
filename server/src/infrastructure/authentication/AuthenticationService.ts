import FirebaseAuthenticationService from "./firebase/FirebaseAuthenticationService";

export type AuthenticationResult = {
    userId: string;
    email: string | null;
    token: string;
}

export type User = {
    userId: string,
    email: string | null,
    token: string
}

export interface AuthenticationService {
      loginWithEmailPassword(email: string, password: string): Promise<AuthenticationResult>;
      logout(): Promise<object>;
      validateToken(token: string, refresh: boolean): Promise<User>;
}

export const authenticationService = new FirebaseAuthenticationService();