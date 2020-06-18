import { AuthenticationService, AuthenticationResult, ChangePasswordCommand, ChangePasswordResult, CompleteResetPasswordCommand } from "../AuthenticationService";
import { FirebaseConfig } from "./FirebaseConfiguration";
import jwt from 'jsonwebtoken';
const TOKEN_EXPIRED_ERROR_CODE = "auth/id-token-expired";

// This import loads the firebase namespace.
import firebase from 'firebase/app';

import requestPromise from 'request-promise';
import InvalidValueError from "../../users/InvalidValueError";
import { UserRepository } from "../../users/UserService";
import { UserSessionRepository } from "./UserSessionRepository";
import UserNotFoundError from "../../users/UserNotFoundError";

type RefreshTokenResponse = {
    refreshToken: string,
    newToken: string
}

export type UserData = {
    email: string,
    userId: string,
    tokenExpirationTime: number
}

export default class FirebaseAuthenticationService implements AuthenticationService {

    private readonly firebaseInstance: FirebaseConfig;
    private readonly userRepository: UserRepository;
    private readonly userSessionRepository: UserSessionRepository;

    constructor(firebaseInstance: FirebaseConfig, 
                userRepository: UserRepository, 
                userSessionRepository: UserSessionRepository) {
        this.firebaseInstance = firebaseInstance;
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
    }


    // Need to both wrap in new Promise() + use regular promise.then instead of just async/await 
    // due to issue with Firebase.auth library:
    // - see https://github.com/firebase/firebase-js-sdk/issues/1881
    async loginWithEmailPassword(email: string, password: string): Promise<AuthenticationResult> {

        return new Promise((resolve, reject) => {
            return this.firebaseInstance
                .get()
                .auth()
                .signInWithEmailAndPassword(email, password)
                .then(async (userCredential: firebase.auth.UserCredential) => {
                    if (userCredential.user) {
                        const userId = userCredential.user.uid;
                        const email = userCredential.user.email;
                        const emailVerified = userCredential.user.emailVerified;

                        //TODO: check custom status in Firestore?
                        if (!emailVerified) {
                           reject(new Error("User cannot login until email is verified")); 
                        }

                        const token = await this.tokenFromUser(userCredential.user);
                        if (token) {
                            //TODO: cache the token for this user locally for 1h: #191
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
            //TODO: validate the token first, if invalid, the logout should be rejected
            await this.firebaseSignOut();
            const userData = this.userDataFromToken(token);
            await this.userSessionRepository.removeUserSession(userData, token);
            const result = this.revokeRefreshTokens(userData.userId);
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
        try {

            let decodedToken = await this.verifyIdToken(token);
            const currentUser = await this.userSessionRepository.findCurrentUser(decodedToken.uid);

            if (refresh) {
                console.log("Refresh requested");
                const currentUser = await this.userSessionRepository.findCurrentUser(decodedToken.uid);
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
                    await this.userSessionRepository.saveCurrentUser(refreshedUser.userId, refreshedUser);
                    return { email: refreshedUser.email, userId: refreshedUser.userId, token: refreshResult.newToken };
                } else {
                    throw new Error('Error validating token, cannot find current session and refresh token, should re-authenticate');
                }
            } else {
                if (currentUser) {
                    const updatedSession =  {
                        token,
                        refreshToken: currentUser.refreshToken,
                        email: decodedToken.email,
                        userId: decodedToken.uid,
                        lastLogin: new Date()
                    }
                    
                    this.userSessionRepository.saveCurrentUser(decodedToken.uid, updatedSession);
                    return { token, email: decodedToken.email, userId: decodedToken.uid };
                } else {
                    throw new Error("Error validating token, no user is present for valid token, should re-login");
                }
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

    /**
     * For authenticated users, given the authentication happened recently, change the password
     * to the provided one. If authentication happened too long ago, reauthentication needs to happen again
     * in order to be able to change the password
     * 
     * see: https://stackoverflow.com/questions/39866086/change-password-with-firebase-for-android
     * 
     * @param changePasswordCommand the userId, current password, new password, and repeated new password
     */
    async changePassword(changePasswordCommand: ChangePasswordCommand): Promise<ChangePasswordResult> {
        try {

            this.validatePasswords(changePasswordCommand.newPassword, changePasswordCommand.newPasswordRepeated);

            const firebaseAdminAuth = this.firebaseInstance.getAdmin().auth();
        
            // 1. re-authenticate first
            const loginResult = await this.loginWithEmailPassword(changePasswordCommand.email, changePasswordCommand.oldPassword);
        
            // 2. change to new password
            const userRecord = await firebaseAdminAuth.updateUser(loginResult.userId, { password: changePasswordCommand.newPassword });
            console.log("Successfully updated user password", userRecord.toJSON());

            // 3. invalidate sessions/logout (everywhere if in multiple places)
            await this.logout(loginResult.token);

            return { result: "ok" };

        } catch (err) {
            console.log("Error changing password", err);
            throw new Error("Error changing password");
        }
    }

    /**
     * Request a password reset for the user with the provided email.
     * This action involves sending a 'resetPassword' email to the address indicated if
     * there is an existing user in the system with the given address
     * 
     * @param email 
     */
    async resetPasswordRequest(email: string): Promise<object> {
        try {
            this.validateEmail(email);
            const user = await this.userRepository.byEmail(email);
            console.log(`Found user ${user.userId} with email ${email}`);
            const auth: firebase.auth.Auth = this.firebaseInstance.get().auth();
            await auth.sendPasswordResetEmail(email);
            console.log(`Reset password email sent to ${email}`);
            return { result: "ok" };
        } catch (err) {
            console.log("Error requesting reset password", err);
            if (err instanceof UserNotFoundError) {
                console.log(`User not found with email: ${email}`, email);
                throw err;
            }
            throw new Error("Error requesting reset password");
        }
    }

    /**
     * Complete an initiated reset password action for a user. This action requires the reset action code
     * sent to the user's email address to be valid. If so, the user's password gets updated 
     * to the new provided one.
     * 
     * @param completeResetPasswordCmd the email address, action code and new password
     * @see https://firebase.google.com/docs/reference/js/firebase.auth.Auth#confirmPasswordReset 
     * @see https://firebase.google.com/docs/reference/js/firebase.auth.Auth#verifyPasswordResetCode
     * 
     */
    async completeResetPassword(completeResetPasswordCmd: CompleteResetPasswordCommand): Promise<object> {
        
        const { email, resetCode, newPassword } = completeResetPasswordCmd;

        try  {
            
            this.validateEmail(email);

            if (!newPassword) {
                throw new InvalidValueError("A new password must be provided");
            }

            const user = await this.userRepository.byEmail(email);
            console.log(`Found user ${user.userId} with email ${email} to complete password reset`);
            console.log("Reset code is " + resetCode);
            const auth: firebase.auth.Auth = this.firebaseInstance.get().auth();
            await auth.verifyPasswordResetCode(resetCode);
            console.log(`Verify password code for user ${user.userId} with email ${email} is correct`);

            await auth.confirmPasswordReset(resetCode, newPassword);
            console.log(`Password successfully reset for user ${user.userId} with email ${email}`);

            return { result: "ok" };

        } catch (err) {
            console.log("Error completing password reset for user", err);
            if (err instanceof UserNotFoundError) {
                console.log(`User not found with email: ${email}`, email);
                throw err;
            }
            throw new Error("Error completing password reset for user");
        }
    }

    private validatePasswords(password: string, repeatedPassword: string) {
        if (!password || !repeatedPassword) {
            throw new InvalidValueError(`Password is invalid`);
        }

        if (password !== repeatedPassword) {
            throw new InvalidValueError(`Passwords don't match`);
        }
    }

    private validateEmail(email: string) {
        if (!email || !(email.indexOf("@") > -1)) {
            throw new InvalidValueError(`Email is invalid: ${email}`);
        }

    }

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
    private async firebaseSignOut() {
        return new Promise((resolve, reject) => {
            return this.firebaseInstance.get().auth().signOut()
                .then(() => resolve({ result: "ok"}))
                .catch((err: Error) => reject(err));
        });
    }

    private async revokeRefreshTokens(userId: string) {
        return new Promise((resolve, reject) => {
            return this.firebaseInstance.getAdmin()
                                   .auth()
                                   .revokeRefreshTokens(userId)
                                   .then(() => resolve({result: "ok"}))
                                   .catch((err: Error)=> reject(err));
        });
    }

    private async verifyIdToken(token: string): Promise<any> {
        return new Promise((resolve, reject) => {
            return this.firebaseInstance
            .getAdmin()
            .auth()
            // checks if revoked, no need to use custom userRepository.checkRevoked
            .verifyIdToken(token, true) 
            .then((decodedToken: any) => resolve(decodedToken))
            .catch((err: Error)=> reject(err));
        });
    }

    private async refreshFromExpiredToken(token: string): Promise<AuthenticationResult> {
        const userData = this.userDataFromToken(token);
        console.log("UserId from token", userData.userId);
        const currentUserData: any = await this.userSessionRepository.findCurrentUser(userData.userId);
        //TODO: check expiration date (created issue #189)
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
        // for #189 exp: The ID token's expiration time, in seconds since the Unix epoch. 
        return { userId: decodedToken.user_id, email: decodedToken.email, tokenExpirationTime: decodedToken.exp };
    }

    async saveCurrentUser(token: string): Promise<any> {
        console.log("Saving current user");
        return await this.firebaseInstance.get().auth().onAuthStateChanged(async (currentUser: firebase.User) => {
            if (currentUser) {
                console.log("Saving current user");
                const user = {
                    userId: currentUser.uid,
                    email: currentUser.email,
                    token: token,
                    refreshToken: currentUser.refreshToken,
                    lastLogin: new Date()
                };
                return this.userSessionRepository.saveCurrentUser(currentUser.uid, user);
            } else {
                console.log("Current user not found, user not logged in");
            }
        });
    }

    private async refreshToken(refreshToken: string): Promise<RefreshTokenResponse> {
        return await this.exchangeRefreshTokenWithIdToken(refreshToken);
    }

    private async exchangeRefreshTokenWithIdToken(refreshToken: string): Promise<RefreshTokenResponse> {
        const options = {
            method: 'POST',
            uri: `https://securetoken.googleapis.com/v1/token?key=${this.firebaseInstance.apiKey()}`,
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

/* Firebase stores last user login and more metadata using Admin SDK:
{ uid: '',
  email: 'moneycoltestuser1@mailinator.com',
  emailVerified: true,
  displayName: 'dafe Dafe52',
  photoURL: undefined,
  phoneNumber: undefined,
  disabled: false,
  metadata:
   { lastSignInTime: 'Tue, 23 Jun 2020 07:09:06 GMT', <-- useful for 'recent sign in'
     creationTime: 'Fri, 12 Jun 2020 06:52:17 GMT' },
  passwordHash: undefined,
  passwordSalt: undefined,
  customClaims: undefined,
  tokensValidAfterTime: 'Tue, 23 Jun 2020 07:09:07 GMT',
  tenantId: undefined,
  providerData:
   [ { uid: 'moneycoltestuser1@mailinator.com',
       displayName: 'dafe Dafe52',
       email: 'moneycoltestuser1@mailinator.com',
       photoURL: undefined,
       providerId: 'password',
       phoneNumber: undefined } ] }
*/