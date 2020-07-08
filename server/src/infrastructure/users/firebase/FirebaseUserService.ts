import { UserService, CreateUserCommand, UserCreatedResult, 
        UserStatus, UserRepository, EmailVerificationCommand, EmailVerificationResult, 
        EmailService, UpdateUserProfileCommand, UserProfileResult, User } from "../UserService";
import { FirebaseConfig } from '../../authentication/firebase/FirebaseConfiguration';
import InvalidValueError from "../InvalidValueError";

// These imports load individual services into the firebase namespace.
import 'firebase/auth';
import 'firebase/database';
import 'firebase/firestore';

import UserInInvalidStateError from "../UserInInvalidStateError";

const ACCOUNT_DISABLED_ERROR_CODE = 'auth/user-disabled';

class FirebaseUserService implements UserService {
  
    private firebaseInstance: FirebaseConfig;
    private userRepository: UserRepository;
    private emailService: EmailService;


    constructor(firebaseInstance: FirebaseConfig, 
                userRepository: UserRepository, 
                emailService: EmailService) {
        this.firebaseInstance = firebaseInstance;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Signs up a user in the system provided email and password using Firebase Auth.
     * 
     * The parameters are validated and the user is created both in Firebase Auth and Firestore. 
     * After the values are persisted a verification email is sent asynchronously, keeping the user in 
     * PENDING_VERIFICATION state until the verification step completes (@see verifyUserEmail).
     * 
     * The newly created user won't be able to login until email verification happens.
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
            //TODO: how does this work concurrently (multiple users created at once)
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
            const actionCodeSettings = {
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
                //TODO: there are certain errors on firebase that need to bubble up or wrap
                // as they have specific codes and are informational. Need handler to wrap these errors
                // properly
                if (err.code) {
                    throw err;
                } else {
                    throw new Error("Error creating user");    
                }
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

    /**
     * Update the user profile with the passed in data. The user must be logged in and this data
     * cannot be email or password (specific flows)
     * 
     * @param updateProfileCommand 
     */
    async updateUserProfile(updateProfileCmd: UpdateUserProfileCommand): Promise<UserProfileResult> {
        
        if (!updateProfileCmd.userId) {
            throw new InvalidValueError("userId must be present to update user profile");
        }

        const { userId, firstName, lastName, username } = updateProfileCmd;
        const savedUser = await this.userRepository.byId(userId);

        if (savedUser.status != UserStatus.ACTIVE) {
            throw new UserInInvalidStateError("Cannot update user profile in non-active user");
        }

        //TODO: should update in Firebase as well using Admin SDK updateUser
        const userToUpdate = {...savedUser, firstName, lastName, username };
        const updatedUserResult = await this.userRepository.updateUserData(userToUpdate);
        console.log("Updated user", updatedUserResult);
        
        return updateProfileCmd as UserProfileResult;
    }

    /**
     * Find the user profile data for the given userId
     *  
     * @param userId The id of the user to get profile information for
     */
    async findUserProfile(userId: string): Promise<UserProfileResult> {
        
        const { email, username, firstName, lastName, status } = await this.userRepository.byId(userId);

        if (status != UserStatus.ACTIVE) {
            throw new UserInInvalidStateError("Cannot get user profile for non-active user");
        }

        return { userId, email, username, firstName, lastName };
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

        this.validatePasswords(userToCreate.password, userToCreate.repeatedPassword);
    }

    private validatePasswords(password: string, repeatedPassword: string) {
        if (!password || !repeatedPassword) {
            throw new InvalidValueError(`Password is invalid`);
        }

        if (password !== repeatedPassword) {
            throw new InvalidValueError(`Passwords don't match`);
        }
    }
}

export { FirebaseUserService };