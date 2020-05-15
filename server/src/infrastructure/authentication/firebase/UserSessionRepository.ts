import FirestoreUserSessionRepository from './FirestoreUserSessionRepository';
import { User } from '../AuthenticationService';

export interface UserSessionRepository {
    saveCurrentUser(userId: string, user: User): Promise<object>;
    findCurrentUser(userId: string):  Promise<User | null> ;
    removeUserSession(userId: string): Promise<any>;
}

export const userSessionRepository: UserSessionRepository = new FirestoreUserSessionRepository();
