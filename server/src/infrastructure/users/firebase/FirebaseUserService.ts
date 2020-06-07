import { UserService, CreateUserCommand, UserCreatedResult, UserStatus, UserRepository, EmailVerificationCommand, EmailVerificationResult, EmailService } from "../UserService";
import { FirebaseConfig } from '../../authentication/firebase/FirebaseConfiguration';
import InvalidValueError from "../InvalidValueError";

// These imports load individual services into the firebase namespace.
import 'firebase/auth';
import 'firebase/database';
import 'firebase/firestore';

const ACCOUNT_DISABLED_ERROR_CODE = 'auth/user-disabled';

class FirebaseUserService implements UserService {

    private firebaseInstance: FirebaseConfig;
    private userRepository: UserRepository;
    private emailService: EmailService;

    constructor(firebaseInstance: FirebaseConfig, userRepository: UserRepository, emailService: EmailService) {
        this.firebaseInstance = firebaseInstance;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Signs up a user provided email and password using Firebase Auth.
     * 
     * The parameters are validated and user is created bot in Firebase Auth and Firestore. 
     * After the values are persisted a verification email is sent asynchronously, keeping the user in 
     * PENDING_VERIFICATION state until the verification step happens.
     * 
     * The newly created user won't be able to login with email verification (by default) 
     * 
     * 
     * @param createUserCommand the required parameters to create the user (username, email, ...)
     */
    async signUpWithEmail(createUserCommand: CreateUserCommand): Promise<UserCreatedResult> {

        try {

            this.validate(createUserCommand);

            const userDisplayName = this.setDisplayName(createUserCommand);
            const email = createUserCommand.email;
            const password = createUserCommand.password;
            const displayName = userDisplayName;

            // This could be a preference in Firestore, global app 'preferences'
            const emailVerified = false;
            const firebaseAuth = this.firebaseInstance.get().auth();

            const result = await firebaseAuth.createUserWithEmailAndPassword(email, password);
            const userId = result.user.uid;
            console.log("Firebase user created: ", userId);

            // Need currentUser to send verification email and update the name
            const currentUser = firebaseAuth.currentUser;

            // Update the displayName for Firebase Auth -- it could be off request too (not await)
            console.log(`User with email ${email} registered. Setting name.`);

            await firebaseAuth.currentUser.updateProfile({ displayName, emailVerified });
            console.log(`User profile with email ${email} name updated. Persisting user.`);

            // Persist in Firestore to control custom workflows, 
            // i.e. won't allow login if user is pending verification and future preferences
            const userToPersist = {
                userId: userId,
                email: email,
                username: createUserCommand.username,
                firstName: createUserCommand.firstName,
                lastName: createUserCommand.lastName,
                status: UserStatus.PENDING_VERIFICATION
            }

            await this.userRepository.persistUser(userToPersist);

            console.log("Sending email verification");

            // this only changes continueURL, so the user can continue in a known place after
            // verifying the email. We also need to set the whole link back to the app too,
            // it's the actionLink in the Firebase Authentication console
            var actionCodeSettings = {
                url: this.emailService.generateComebackUrl(email),
                handleCodeInApp: false,
            };

            currentUser.sendEmailVerification(actionCodeSettings);

            return userToPersist;
        } catch (err) {
            console.log("Error creating user", err)
            if (err instanceof InvalidValueError) {
                throw err;
            } else {
                throw new Error("Error creating user");
            }
        }
    }

    /**
     * Handler to verify a user email using Firebase Auth SDK. This operation is triggered by the user 
     * clicking on the the action URL embedded in the welcome email the user receives upon account creation.
     * 
     * On successful verification, the user is set to active in Firestore and it's then allowed to login
     * 
     * @param emailVerificationCommand the actionCode, continueUrl and language parameters passed through actionURL query parameters
     */
    async verifyUserEmail(emailVerificationCommand: EmailVerificationCommand): Promise<EmailVerificationResult> {

        try {

            const result = await this.emailService.verifyEmail(emailVerificationCommand.actionCode,
                emailVerificationCommand.continueUrl,
                emailVerificationCommand.lang);

            console.log("Email verified result", result);
            const email = result.email;

            const user = await this.userRepository.byEmail(email);
            user.status = UserStatus.ACTIVE;
            await this.userRepository.updateUserData(user);

            return {
                email: email,
                result: "ok",
                comebackUrl: emailVerificationCommand.continueUrl
            };

        } catch (err) {
            console.error("Error verifying email", err);
            throw err;
        }
    }

    private setDisplayName(userCmd: CreateUserCommand) {
        if (userCmd.firstName && userCmd.lastName) {
            return userCmd.firstName + " " + userCmd.lastName;
        }

        if (userCmd.firstName) {
            return userCmd.firstName;
        }

        return userCmd.username;
    }

    private async firebaseAdminUpdateUser(userId: string, displayName: string, emailVerified: boolean, disabled: boolean): Promise<any> {
        return new Promise((resolve, reject) => {
            return this.firebaseInstance.getAdmin().auth().updateUser(userId, { displayName, disabled, emailVerified })
                .then((updatedUser: any) => {
                    console.log("Success", updatedUser);
                    resolve(updatedUser)
                })
                .catch((err: any) => {
                    if (err.code && err.code === ACCOUNT_DISABLED_ERROR_CODE) {
                        console.log("Ignoring disabled account error -- expected on registration");
                        resolve({});
                    } else {
                        reject(err);
                    }
                });
        });
    }

    private validate(userToCreate: CreateUserCommand) {
        if (!userToCreate.username) {
            throw new InvalidValueError(`Username is invalid ${userToCreate.username}`);
        }

        if (!userToCreate.email || !(userToCreate.email.indexOf("@") > -1)) {
            throw new InvalidValueError(`Email is invalid ${userToCreate.email}`);
        }

        if (!userToCreate.password || !userToCreate.repeatedPassword) {
            throw new InvalidValueError(`Password is invalid`);
        }

        if (userToCreate.password !== userToCreate.repeatedPassword) {
            throw new InvalidValueError(`Passwords don't match`);
        }
    }
}

export { FirebaseUserService };