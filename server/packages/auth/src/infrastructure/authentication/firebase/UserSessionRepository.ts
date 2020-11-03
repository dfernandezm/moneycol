import FirestoreUserSessionRepository from './FirestoreUserSessionRepository';
import { AuthUser } from '../AuthenticationService';
import { UserData } from './FirebaseAuthenticationService';

export interface UserSessionRepository {
    saveCurrentUser(userId: string, user: AuthUser): Promise<object>;
    findCurrentUser(userId: string):  Promise<AuthUser | null> ;
    removeUserSession(userData: UserData, token: string): Promise<any>;
    checkRevoked(token: string): Promise<boolean>;
}

export const userSessionRepository: UserSessionRepository = new FirestoreUserSessionRepository();
