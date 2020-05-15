import { AuthenticationService, AuthenticationResult, authenticationService } from "../AuthenticationService";
import { firebaseInstance, FIREBASE_API_KEY } from "./FirebaseConfiguration";
import { tokensCache } from "./TokensCache";
import { userSessionRepository } from "./UserSessionRepository";
import jwt from 'jsonwebtoken';
const TOKEN_EXPIRED_ERROR_CODE = "auth/id-token-expired";

// This import loads the firebase namespace.
import firebase from 'firebase/app';

// These imports load individual services into the firebase namespace.
import 'firebase/auth';
import 'firebase/database';
import 'firebase/firestore';

import requestPromise from 'request-promise';

type RefreshTokenResponse = {
    refreshToken: string,
    newToken: string
}

type UserData = {
    email: string,
    userId: string,
    tokenExpirationTime: number
}

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

    // Need to both wrap in new Promise() + use regular promise.then instead of just async/await 
    // due to issue with Firebase.auth library:
    // - see https://github.com/firebase/firebase-js-sdk/issues/1881
    async loginWithEmailPassword(email: string, password: string): Promise<AuthenticationResult> {
        return new Promise((resolve, reject) => {
            return firebaseInstance
                .get()
                .auth()
                .signInWithEmailAndPassword(email, password)
                .then(async (userCredential: firebase.auth.UserCredential) => {
                    if (userCredential.user) {
                        const userId = userCredential.user.uid;
                        const email = userCredential.user.email;
                        const token = await this.tokenFromUser(userCredential.user);
                        if (token) {
                            //TODO: cache the token for this user locally for 1h
                            const authResult = { email, userId, token };
                            await this.saveCurrentUser(token);
                            return resolve(authResult);
                        } else {
                            console.log("Login error due to invalid or missing token", userCredential.user);
                            reject(new Error("Authentication error: invalid or missing token"));
                        }
                    } else {
                        console.log(`Cannot find user object in userCredential for user with email ${email}`, userCredential);
                        reject(new Error("Authentication error: user not found in userCredential"));
                    }
                }).catch((error: Error) => {
                    console.log("Authentication error login with email/password: ", error);
                    reject(new Error("Authentication error login with email/password: " + error));
                })
        });
    }

    async logout(token: string): Promise<object> {
        try {
            //TODO: will need wrap in promise.then instead of async/await due to firebase.auth issue
            await firebaseInstance.get().auth().signOut();
            const userData = this.userDataFromToken(token);
            await userSessionRepository.removeUserSession(userData.userId);
            const result = await firebaseInstance.getAdmin().auth().revokeRefreshTokens(userData.userId);
            console.log("Revoked refresh", result);
            return {
                result: "ok"
            };
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
        //TODO: will need wrap in promise.then instead of async/await due to firebase.auth issue
        try {

            let decodedToken = await firebaseInstance.getAdmin().auth().verifyIdToken(token);

            if (refresh) {
                console.log("Refresh requested");
                const currentUser = await userSessionRepository.findCurrentUser(decodedToken.uid);
                if (currentUser) {
                    console.log("Found existing user, refreshToken is", currentUser.refreshToken);
                    const refreshResult = await this.refreshToken(currentUser.refreshToken);
                    const refreshedUser = {
                        token: refreshResult.newToken,
                        userId: currentUser.userId,
                        email: decodedToken.email,
                        refreshToken: refreshResult.refreshToken,
                        lastLogin: new Date()
                    };
                    await userSessionRepository.saveCurrentUser(refreshedUser.userId, refreshedUser);
                    return { email: refreshedUser.email, userId: refreshedUser.userId, token: refreshResult.newToken };
                } else {
                    throw new Error('Error validating token, cannot refresh current token, should re-authenticate');
                }
            } else {
                return { token, email: decodedToken.email, userId: decodedToken.uid };
            }

        } catch (err) {
            console.log("Error Code:", err.code);
            if (err.code === TOKEN_EXPIRED_ERROR_CODE && refresh) {
                console.log("Token expired, refreshing requested");
                return await this.refreshFromExpiredToken(token);
            } else {
                console.log('Error validating token', err.message);
                throw new Error('Error validating token');
            }
        }
    }

    private async refreshFromExpiredToken(token: string): Promise<AuthenticationResult> {
        const userData = this.userDataFromToken(token);
        console.log("UserId from token", userData.userId);
        const currentUserData: any = await userSessionRepository.findCurrentUser(userData.userId);
        //TODO: check expiration date checks (issue #)
        // - the session creation should be AFTER the expiration time
        // - the token expiration time - session creation time > session_duration (1 month?)
        if (currentUserData) {
            const refreshedTokenResponse = await this.refreshToken(currentUserData.refreshToken);
            return { token: refreshedTokenResponse.newToken, email: userData.email, userId: userData.userId };
        } else {
            throw new Error("No user session found, should re-login");
        }
    }

    private userDataFromToken(token: string): UserData {
        const decodedToken: any = jwt.decode(token);
        //exp: The ID token's expiration time, in seconds since the Unix epoch. 
        return { userId: decodedToken.user_id, email: decodedToken.email, tokenExpirationTime: decodedToken.exp };
    }

    async saveCurrentUser(token: string): Promise<any> {
        console.log("Saving current user");
        return await firebaseInstance.get().auth().onAuthStateChanged(async (currentUser: firebase.User) => {
            if (currentUser) {
                console.log("Saving current user");
                const user = {
                    userId: currentUser.uid,
                    email: currentUser.email,
                    token: token,
                    refreshToken: currentUser.refreshToken,
                    lastLogin: new Date()
                };
                return userSessionRepository.saveCurrentUser(currentUser.uid, user);
            } else {
                console.log("Current user not found, user not logged in");
            }
        });
    }

    // Reauthentication: https://stackoverflow.com/questions/38233687/how-to-use-the-firebase-refreshtoken-to-reauthenticate
    // It should use: https://stackoverflow.com/questions/56583184/what-is-the-best-way-to-use-async-await-inside-onauthstatechanged-of-firebase/56583572
    private async refreshToken(refreshToken: string): Promise<RefreshTokenResponse> {
        return await this.exchangeRefreshTokenWithIdToken(refreshToken);
    }

    // https://stackoverflow.com/questions/38233687/how-to-use-the-firebase-refreshtoken-to-reauthenticate
    // https://firebase.google.com/docs/reference/rest/auth/
    private async exchangeRefreshTokenWithIdToken(refreshToken: string): Promise<RefreshTokenResponse> {
        const options = {
            method: 'POST',
            uri: `https://securetoken.googleapis.com/v1/token?key=${FIREBASE_API_KEY}`,
            body: `grant_type=refresh_token&refresh_token=${refreshToken}`,
            headers: {
                'content-type': 'application/x-www-form-urlencoded'
            }
        };

        try {
            const response = await requestPromise(options);
            const resp = JSON.parse(response);
            return { refreshToken: resp.refresh_token, newToken: resp.id_token };
        } catch (err) {
            console.log("Error exchanging token", err.message);
            throw err;
        }
    }
}