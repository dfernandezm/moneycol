import FirebaseAuthenticationService, { UserData } from "../dist/infrastructure/authentication/firebase/FirebaseAuthenticationService";
import { FirebaseConfig, userRepository, User, UserRepository, UserCreatedResult, UserStatus } from "@moneycol-server/users";
import { UserSessionRepository } from "../dist/infrastructure/authentication/firebase/UserSessionRepository";
import { AuthUser } from "../src/infrastructure/authentication/AuthenticationService";

describe('FirebaseAuthenticationService', () => {

    let mockFirebaseConfig: FirebaseConfig;

    beforeEach(() => {

        jest.clearAllMocks();

    });

    // https://stackoverflow.com/questions/61391590/how-to-mock-firebase-auth-usercredential-jest/61400492#61400492
    const setupUserCredentialMock = (userId: string, email: string, emailVerified: boolean) => {
        return {
            user: {
                uid: userId, email, emailVerified, getIdToken: jest.fn(() => "testToken")
            },
            credential: null
        }
    }

    test('logs with email and password successfully obtaining token', async () => {

        const userId = "testUserId";
        const email = "test@user.com";
        const password = "pass";
        const emailVerified = true;
        const username = "testUsername";
        const firstName = "firstName";
        const lastName = "lastName";
        const testToken = "testToken";

        const userCredentialMock = setupUserCredentialMock(userId, email, emailVerified)
        const userCredentialMockFn = jest.fn(async () => userCredentialMock);
        const userCreated: User = {
            userId, username, email, firstName, lastName, status: UserStatus.ACTIVE
        }

        mockFirebaseConfig = setupFirebaseMock(userCredentialMockFn);
        const userRepositoryMock = setupUserRepositoryMock(userCreated);

        const user: AuthUser = anUser(testToken, userId)

        const userSessionRepositoryMock = setupUserSessionRepositoryMock(user);
        const authService = new FirebaseAuthenticationService(mockFirebaseConfig, userRepositoryMock, userSessionRepositoryMock);

        const authResult = await authService.loginWithEmailPassword(email, password);

        expect(authResult).toEqual({ email, userId, token: testToken })
        expect(userCredentialMock.user.getIdToken).toHaveBeenCalled()

    });

    test('fails with too many login attempts', async () => {

        const errorCode = 'auth/too-many-requests'
        const errorMessage = 'Access to this account has been temporarily disabled due to many failed login attempts. You can immediately restore it by resetting your password or you can try again later.';

        const errorThrown = (errCode:string , errMessage: string) => Promise.reject({errCode, errMessage});
        const errorThrownMock = jest.fn(() => errorThrown(errorCode, errorMessage))

        mockFirebaseConfig = setupFirebaseMock(errorThrownMock)

        const user: AuthUser = {
            token: "",
            refreshToken: "",
            email: "",
            provider: "",
            userId: "userId",
            lastLogin: new Date()
        }

        const userId = "testUserId";
        const email = "test@user.com";
        const password = "pass";
        const username = "testUsername";
        const firstName = "firstName";
        const lastName = "lastName";

        const userCreated: User = {
            userId, username, email, firstName, lastName, status: UserStatus.ACTIVE
        }

        const userRepositoryMock = setupUserRepositoryMock(userCreated);

        const userSessionRepositoryMock = setupUserSessionRepositoryMock(user);
        const authService = new FirebaseAuthenticationService(mockFirebaseConfig, userRepositoryMock, userSessionRepositoryMock);

        try {
            await authService.loginWithEmailPassword(email, password);
        } catch(error) {
            expect(error.errCode).toEqual(errorCode);
            expect(error.errMessage).toEqual(errorMessage);
        }
    });

    const setupUserRepositoryMock = (user: User) => {
        return {
            persistUser: jest.fn((user: User) => Promise.resolve({})),
            byId: jest.fn(async () => user),
            byEmail: jest.fn(async () => user),
            updateUserData: jest.fn(() => Promise.resolve({})),
            exists: jest.fn(async () => true)
        }
    }

    const setupUserSessionRepositoryMock = (user: AuthUser) => {
        return {
            saveCurrentUser: jest.fn(async () => Promise.resolve({})),
            findCurrentUser: jest.fn(async () => Promise.resolve(user)),
            removeUserSession: jest.fn(async () => { }),
            checkRevoked: jest.fn(async () => false),
            findCurrentUserById: jest.fn(async () => Promise.resolve(user)),
            findCurrentUserByEmail: jest.fn(async () => Promise.resolve(user))
        }
    }

    const setupFirebaseMock = (signInWithEmailAndPasswordMock: jest.Mock): FirebaseConfig => {
        let mockFirebaseConfig = new FirebaseConfig();
        mockFirebaseConfig.get = () => {
            return {
                auth: () => {
                    return {
                        signInWithEmailAndPassword: signInWithEmailAndPasswordMock,
                        onAuthStateChanged: jest.fn(() => { })
                    }
                }
            }
        }

        return mockFirebaseConfig
    }

});

function anUser(testToken: string, userId: string): AuthUser {
    return {
        token: testToken,
        refreshToken: "",
        email: "",
        provider: "",
        userId: userId,
        lastLogin: new Date()
    };
}
