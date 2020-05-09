import { AuthenticationService, AuthenticationResult, authenticationService } from "../AuthenticationService";
import { firebaseInstance } from "./FirebaseConfiguration";
import { tokensCache } from "./TokensCache";
import jwt from 'jsonwebtoken';
const TOKEN_EXPIRED_ERROR_CODE = "auth/id-token-expired";

export default class FirebaseAuthenticationService implements AuthenticationService {

    private async tokenFromUser(user: firebase.User | null) {
        if (user) {
            try {
                const idToken = await user.getIdToken(true);
                return idToken;
            } catch (error) {
                console.log("Error retrieving token: ", error);
                return null;
            }
        }
    }

    async loginWithEmailPassword(email: string, password: string): Promise<AuthenticationResult> {
        try {
            let userCredential: firebase.auth.UserCredential = await firebaseInstance
                .get()
                .auth()
                .signInWithEmailAndPassword(email, password);

            if (userCredential.user) {
                const userId = userCredential.user.uid;
                const email = userCredential.user.email;
                const token = await this.tokenFromUser(userCredential.user);
                if (token) {
                    //TODO: cache the token for this user locally for 1h
                    const authResult = { email, userId, token };
                    tokensCache.cacheToken(userId, token);
                    await this.cacheCurrentUser();
                    return authResult;
                } else {
                    console.log("Login error due to invalid or missing token", userCredential.user);
                    throw new Error("Authentication error: invalid or missing token");
                }
            } else {
                console.log(`Cannot find user object in userCredential for user with email ${email}`, userCredential);
                throw new Error("Authentication error: user not found in userCredential");
            }
        } catch (error) {
            console.log("Authentication error login with email/password: ", error);
            throw new Error("Authentication error login with email/password: " + error);
        }
    }

    async logout(): Promise<object> {
        try {
            firebaseInstance.get().auth().signOut();
            return {
                result: "ok"
            }
        } catch (error) {
            console.log("Logout error: ", error);
            throw new Error("Logout error: " + error);
        }
    }

    /**
     * This can be used for 2 purposes:
     * 
     * - To validate the token that gets sent before invoking APIs resolvers use (willSendRequest in datasource)
     * - To verify the token the frontend has in local storage and refresh it, only if it's valid but has expired
     * 
     * @param token 
     * @param refresh 
     */
    async validateToken(token: string, refresh: boolean = false): Promise<AuthenticationResult> {
        try {
            let decodedToken = await firebaseInstance.getAdmin().auth().verifyIdToken(token);
            await this.restoreCurrentUser(decodedToken.uid);
            if (refresh) {
                console.log("Refresh requested");
                const refreshResult = await this.refreshToken();
                console.log("Refreshed token result", refreshResult);
                return refreshResult;
            } else {
                return { token, email: decodedToken.email, userId: decodedToken.uid };
            }

        } catch (err) {
            console.log("Code:", err.code);
            if (err.code === TOKEN_EXPIRED_ERROR_CODE && refresh) {
                console.log("Token expired, refreshing requested");
                return await this.refreshToken();
            } else {
                console.log('Error validating token', err);
                throw new Error('Error validating token');
            }
        }
    }

    async restoreCurrentUser(userId: string) {
        const user = tokensCache.getCurrentUserFromCache(userId);
        if (user) {
            let result = await firebaseInstance.get().auth().updateCurrentUser(user);
            console.log("Restored currentUser");
        } else {
            console.log("User not in cache, cannot be restored");
        }
    }

    async cacheCurrentUser(): Promise<any> {
        console.log("Caching current user");
        return await firebaseInstance.get().auth().onAuthStateChanged(async (currentUser: any) => {
            if (currentUser) {
                console.log("Caching current user");
                tokensCache.cacheCurrentUser(currentUser.uid, currentUser);
            } else {
                console.log("Current user not found, user not logged in");
            }
        });
    }

    // Reauthentication: https://stackoverflow.com/questions/38233687/how-to-use-the-firebase-refreshtoken-to-reauthenticate
    // TODO: It should use: https://stackoverflow.com/questions/56583184/what-is-the-best-way-to-use-async-await-inside-onauthstatechanged-of-firebase/56583572
    async refreshToken(): Promise<AuthenticationResult> {
        try {
            const currentUser = await firebaseInstance.get().auth().currentUser;
            if (currentUser) {
                let newToken = await currentUser.getIdToken(true);
                console.log("NewToken", newToken);
                const authResult = { token: newToken, userId: currentUser.uid, email: currentUser.email };
                tokensCache.cacheToken(currentUser.uid, newToken);
                await tokensCache.cacheCurrentUser(currentUser.uid, currentUser);
                return authResult;
            } else {
                console.log("Current user not found, please log back in");
                throw new Error("Not logged in");
            }
        } catch (err) {
            console.log('Error generating new token token', err);
            throw err;
        }
    }


    // This should be done only in the cases that tokens / user-centric functionality is required
    readToken(token: string) {

        //     //TODO: should verify the signature or just forward instead of decoding
        //     //see: https://firebase.google.com/docs/auth/admin/verify-id-tokens
        //     const decoded: any = jwt.decode(token);

        //     /*
        //     { iss: 'https://securetoken.google.com/moneycol',
        //       aud: 'moneycol',
        //       auth_time: 1586519807,
        //       user_id: '3eiK7CqInPbgcw1LYq1S8sJqGLy2',
        //       sub: '...',
        //       iat: 1586523227,
        //       exp: 1586526827,
        //       email: 'morenza@gmail.com',
        //       email_verified: true,
        //       firebase:
        //       { identities: { email: [Array] }, sign_in_provider: 'password' } }
        //      */
        //     if (decoded && decoded.aud == "moneycol") {
        //       console.log(`Valid token has been received, user ID is: ${decoded.user_id}`);
        //       return {
        //         email: decoded.email,
        //         userId: decoded.uid
        //       }
        //     } else {
        //       throw new AuthenticationError("Invalid token has been provided");
        //     };
    }
}