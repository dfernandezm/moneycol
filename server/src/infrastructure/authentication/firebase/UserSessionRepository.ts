import FirestoreUserSessionRepository from './FirestoreUserSessionRepository';

export interface UserSessionRepository {
    //TODO: in the interface we should probably use object or some adapter to avoid using firebase 
    saveCurrentUser(userId: string, user: firebase.User): Promise<object>;
    findCurrentUser(userId: string):  Promise<{userId: string, refreshToken:string, userObject: object} | null> ;
}

export const userSessionRepository = new FirestoreUserSessionRepository()