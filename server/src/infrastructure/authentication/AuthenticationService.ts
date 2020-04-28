import FirebaseAuthenticationService from "./firebase/FirebaseAuthenticationService";

export type AuthenticationResult = {
    userId: string;
    email: string | null;
    token: string;
}

export type User = {
    userId: string,
    email: string | null
}

export interface AuthenticationService {
      loginWithEmailPassword(email: string, password: string): Promise<AuthenticationResult>;
      logout(): Promise<object>;
      getCurrentUser(): Promise<User | null>;
      validateToken(token: string): User;
}

export const authenticationService = new FirebaseAuthenticationService();