import { firebaseInstance } from "@moneycol-server/users";
import { UserSessionRepository } from "./UserSessionRepository";
import { User } from "../AuthenticationService";
import { UserData, Provider } from "./FirebaseAuthenticationService";

// May need to set up a service account: https://cloud.google.com/firestore/docs/security/iam
export default class FirestoreUserSessionRepository implements UserSessionRepository {

    //TODO: support multiple sessions in the future (up to 5) - new GH issue
    // - save new session
    // - update session
    // - deactivate session
    // token is valid, but user is logged out
    // https://firebase.google.com/docs/firestore/manage-data/add-data#update_elements_in_an_array
    async saveCurrentUser(userId: string, user: User): Promise<object> {
        //TODO: currently should override records with userId/email
        console.log("Saving current session");
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
            console.log(`Found session for user ${userId}`)
            return {
                userId: docData.userId,
                provider: docData.provider || Provider.PASSWORD,
                refreshToken: docData.refreshToken,
                email: docData.email,
                token: docData.token,
                lastLogin: docData.lastLogin
            };
        } else {
            return null;
        }
    }

    async removeUserSession(userData: UserData, token: string): Promise<any> {
        let db = firebaseInstance.getFirestore();

        // Delete this session
        //TODO: there might be more than 1, should delete all of them??
        let docRef = db.collection('sessions').doc(userData.userId);
        let deleteResult = await docRef.delete();

        // add token to revoked ones
        let usedTokensRef = db.collection('logged_out_tokens').doc(token);
        let usedTokenSetResult = await usedTokensRef.set({ token: token, userData: userData});

        console.log("Deleted sessions", deleteResult, usedTokenSetResult);
        return deleteResult;   
    }

    async checkRevoked(token: string): Promise<boolean> {
        let db = firebaseInstance.getFirestore();
        let usedTokensRef = db.collection('logged_out_tokens').doc(token);
        let docSnap = await usedTokensRef.get();
        return docSnap.exists;
    }
}