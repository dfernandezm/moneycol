import FirestoreUserSessionRepository from './FirestoreUserSessionRepository';
import { User } from '../AuthenticationService';
import { UserData } from './FirebaseAuthenticationService';

export interface UserSessionRepository {
    saveCurrentUser(userId: string, user: User): Promise<object>;
    findCurrentUser(userId: string):  Promise<User | null> ;
    removeUserSession(userData: UserData, token: string): Promise<any>;
    checkRevoked(token: string): Promise<boolean>;
}

export const userSessionRepository: UserSessionRepository = new FirestoreUserSessionRepository();
