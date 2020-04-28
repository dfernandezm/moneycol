import { AuthenticationService, AuthenticationResult, authenticationService } from "../AuthenticationService";
import { firebaseInstance } from "./FirebaseConfiguration";
import { User } from "../AuthenticationService";
import jwt from 'jsonwebtoken';

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
                    return { email, userId, token };
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

    //https://stackoverflow.com/questions/51012955/react-firebase-authentication-with-apollo-graphql/53709960
    async getCurrentUser(): Promise<User | null> {
        const user = await firebaseInstance.get().auth().currentUser;
        console.log("Firebase User: ", user);
        if (user === null) {
            return Promise.resolve(null);
        } else {
            return {
                userId: user.uid,
                email: user.email
            };
        }
    }

    //TODO: use the Firebase Admin to validate the token, https://firebase.google.com/docs/admin/setup
    validateToken(token: string): User {

        //TODO: should verify the signature or just forward instead of decoding
        //see: https://firebase.google.com/docs/auth/admin/verify-id-tokens
        const decoded: any = jwt.decode(token);
        
        /*
            { iss: 'https://securetoken.google.com/moneycol',
            aud: 'moneycol',
            auth_time: 1586519807,
            user_id: '3eiK7CqInPbgcw1LYq1S8sJqGLy2',
            sub: '...',
            iat: 1586523227,
            exp: 1586526827,
            email: 'morenza@gmail.com',
            email_verified: true,
            firebase:
            { identities: { email: [Array] }, sign_in_provider: 'password' } }
        */
        if (decoded && decoded.aud == "moneycol") {
            console.log(`Valid token has been received, user ID is: ${decoded.user_id}`);
            return {
                email: decoded.email,
                userId: decoded.user_id
            }
        } else {
            throw new Error("Invalid token has been provided");
        };
    }
}