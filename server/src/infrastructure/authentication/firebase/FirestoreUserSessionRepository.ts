import { firebaseInstance } from "./FirebaseConfiguration";
import { UserSessionRepository } from "./UserSessionRepository";

// May need to set up a service account: https://cloud.google.com/firestore/docs/security/iam
export default class FirestoreUserSessionRepository implements UserSessionRepository {
    async saveCurrentUser(userId: string, user: firebase.User): Promise<object> {
        let db = firebaseInstance.getFirestore();
        let docRef = db.collection('sessions').doc(userId);
        let serializedUser = user.toJSON();

        return docRef.set({
            userId: userId,
            refreshToken: user.refreshToken,
            userObject: serializedUser
        });
    }

    async findCurrentUser(userId: string): Promise<{userId: string, refreshToken:string, userObject: object} | null> {
        let db = firebaseInstance.getFirestore();
        let docRef = db.collection('sessions').doc(userId);
        let docSnapshot = await docRef.get();
        if (docSnapshot.exists) {
            const docData = docSnapshot.data();
            console.log("Found session for user", docData);            
            return { userId: docData.userId, refreshToken: docData.refreshToken, userObject: docData.userObject};
        } else {
            return null;
        }
    }
}