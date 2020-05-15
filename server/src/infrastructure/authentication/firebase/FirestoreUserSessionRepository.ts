import { firebaseInstance } from "./FirebaseConfiguration";
import { UserSessionRepository } from "./UserSessionRepository";
import { User } from "../AuthenticationService";

// May need to set up a service account: https://cloud.google.com/firestore/docs/security/iam
export default class FirestoreUserSessionRepository implements UserSessionRepository {

    async saveCurrentUser(userId: string, user: User): Promise<object> {
        let db = firebaseInstance.getFirestore();
        let docRef = db.collection('sessions').doc(userId);
        return docRef.set({
            userId: userId,
            token: user.token,
            refreshToken: user.refreshToken,
            email: user.email,
            lastLogin: user.lastLogin
        });
    }

    async findCurrentUser(userId: string): Promise<User | null> {
        let db = firebaseInstance.getFirestore();
        let docRef = db.collection('sessions').doc(userId);
        let docSnapshot = await docRef.get();
        if (docSnapshot.exists) {
            const docData = docSnapshot.data();
            console.log("Found session for user", docData);
            return {
                userId: docData.userId,
                refreshToken: docData.refreshToken,
                email: docData.email,
                token: docData.token,
                lastLogin: docData.lastLogin
            };
        } else {
            return null;
        }
    }

    async removeUserSession(userId: string): Promise<any> {
        let db = firebaseInstance.getFirestore();
        let docRef = db.collection('sessions').doc(userId);
        let deleteResult = await docRef.delete();
        console.log("Deleted sessions", deleteResult);
        return deleteResult;   
    }
}