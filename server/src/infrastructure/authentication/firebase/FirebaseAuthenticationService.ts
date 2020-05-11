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
                            await this.saveCurrentUser();
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
                    //return {email: "", userId: "", token: ""};
                    reject(new Error("Authentication error login with email/password: " + error));
                })
        });
    }

    async logout(): Promise<object> {
        try {
            //TODO: will need wrap in promise.then instead of async/await due to firebase.auth issue
            await firebaseInstance.get().auth().signOut();
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
        //TODO: will need wrap in promise.then instead of async/await due to firebase.auth issue
        //TODO: frontend/client should probably send the refresh token as well
        try {

            console.log("Validating token", token);
            let decodedToken = await firebaseInstance.getAdmin().auth().verifyIdToken(token);
            console.log("DecodedToken", decodedToken);
            const currentUser = await userSessionRepository.findCurrentUser(decodedToken.uid);

            if (refresh && currentUser) {
                console.log("Refresh requested, refreshToken is", currentUser.refreshToken);
                const refreshResult = await this.refreshToken(currentUser.refreshToken);
                console.log("Refreshed token result", refreshResult);
                return { token: refreshResult.token, userId: currentUser.userId, email: decodedToken.email };
            } else {
                return { token, email: decodedToken.email, userId: decodedToken.uid };
            }

        } catch (err) {
            console.log("Error Code:", err.code);
            if (err.code === TOKEN_EXPIRED_ERROR_CODE && refresh) {
                console.log("Token expired, refreshing requested");
                const userData = this.userDataFromToken(token);
                console.log("User Id from token", userData.userId);
                const currentUserData: any = await userSessionRepository.findCurrentUser(userData.userId);
                const refreshedTokenResponse = await this.refreshToken(currentUserData.refreshToken);
                return { token: refreshedTokenResponse.token, email: userData.email, userId: userData.userId };
            } else {
                console.log('Error validating token', err.message);
                throw new Error('Error validating token');
            }
        }
    }

    private userDataFromToken(token: string): { userId: string, email: string } {
        const decodedToken: any = jwt.decode(token);
        return { userId: decodedToken.user_id, email: decodedToken.email };
    }

    async saveCurrentUser(): Promise<any> {
        console.log("Saving current user");
        return await firebaseInstance.get().auth().onAuthStateChanged(async (currentUser: any) => {
            if (currentUser) {
                console.log("Saving current user");
                return userSessionRepository.saveCurrentUser(currentUser.uid, currentUser);
            } else {
                console.log("Current user not found, user not logged in");
            }
        });
    }

    // Reauthentication: https://stackoverflow.com/questions/38233687/how-to-use-the-firebase-refreshtoken-to-reauthenticate
    // TODO: It should use: https://stackoverflow.com/questions/56583184/what-is-the-best-way-to-use-async-await-inside-onauthstatechanged-of-firebase/56583572
    private async refreshToken(refreshToken: string): Promise<{ refreshToken: string, token: string }> {
        return await this.exchangeRefreshTokenWithIdToken(refreshToken);
        // console.log(">>>>>> exchangeTokenResponse <<<<<<<", exchangeTokenResponse);
        // return { token: exchangeTokenResponse.idToken, refreshToken };
    }

    // https://stackoverflow.com/questions/38233687/how-to-use-the-firebase-refreshtoken-to-reauthenticate
    // https://firebase.google.com/docs/reference/rest/auth/
    private async exchangeRefreshTokenWithIdToken(refreshToken: string): Promise<{ refreshToken: string, token: string }> {
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
            console.log("Exchanged token", response);
            const resp = JSON.parse(response);
            console.log("Token resp:", resp);
            return { refreshToken: resp.refresh_token, token: resp.id_token };
        } catch (err) {
            console.log("Error exchanging token", err.message);
            throw err;
        }
    }
}