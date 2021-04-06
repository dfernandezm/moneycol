//Consider: https://typegraphql.ml/
//https://www.compose.com/articles/use-all-the-databases-part-2/#elasticsearch

import { IResolvers } from 'graphql-tools';
import { SearchService } from './infrastructure/search/SearchService';
import { ElasticSearchService } from './infrastructure/search/ElasticSearchService';
import { SearchResult } from './infrastructure/search/SearchResult';
import { BankNoteCollection } from './infrastructure/search/SearchResult';
import { NewCollectionInput } from './infrastructure/search/SearchResult';
import { AddBankNoteToCollection } from './infrastructure/search/SearchResult';
import { UpdateCollectionInput } from './infrastructure/search/SearchResult';
import { BankNote } from './types/BankNote';
import decorator from './decorator';
import { CollectionApiResult } from "./infrastructure/collections/types";
import { CollectionsRestDatasource } from './infrastructure/collections/CollectionsRestDatasource';

// Authentication
import { AuthenticationError, ValidationError, ForbiddenError, ApolloError } from 'apollo-server-express';
import { authenticationService, AuthenticationResult, ChangePasswordCommand, ChangePasswordResult, CompleteResetPasswordCommand } from '@moneycol-server/auth';

// Users
import { CreateUserCommand, UserCreatedResult, EmailVerificationResult, VerifyEmailInput, UpdateUserProfileCommand, UserProfileResult } from '@moneycol-server/users';
import { userService, InvalidValueError } from '@moneycol-server/users';

// Support
import { resolverHelper } from './infrastructure/ResolverHelper';
import { InvalidPasswordError } from './InvalidPasswordError';
import { ErrorCodes } from './errorCodes';
import { GeneralError } from './GeneralError';

const searchService: SearchService = new ElasticSearchService();

const resolverMap: IResolvers = {
    Query: {
        
        async search(_: void, args: { term: string, from: number, to: number }, ctx): Promise<SearchResult> {
            return searchService.search("en", args.term, args.from, args.to);
        },

        async collectionData(_: void, args: { collectionId: string }, ctx): Promise<BankNoteCollection> {
            let col: CollectionApiResult = await ctx.dataSources.collectionsAPI.getCollectionById(args.collectionId);
            // These collections are returned without items
            return new BankNoteCollection(col.id, col.name, col.description, col.collectorId, []);
        },

        async collectionsForCollector(_: void, args: { collectorId: string }, { dataSources: { collectionsAPI }}): Promise<BankNoteCollection[]> {
            try {
                
                let collections: CollectionApiResult[] = await collectionsAPI.getCollectionsForCollector(args.collectorId);
                console.log("Collections returned", collections);
                // These collections are returned without items
                let bankNoteCollections = collections.map(col => new BankNoteCollection(col.id, col.name, col.description, col.collectorId, [])); 
                console.log("Resolver: collectionsForCollector\n", collections);
                return bankNoteCollections;
            } catch (err) {
                throw handleErrors(err, "collectionsForCollector");
            }
        },

        async itemsForCollection(_: void, { collectionId }, { dataSources: { collectionsAPI } }): Promise<BankNoteCollection> {
            return decorateBanknoteCollection(collectionId, collectionsAPI)
        },

        async findUserProfile(_: void, args: { userId: string }, ctx): Promise<UserProfileResult> {
            try {
                const userProfile = await userService.findUserProfile(args.userId);
                console.log("Resolver: userProfile\n", userProfile);
                return userProfile;
            } catch (err) {
                throw handleErrors(err, "findUserProfile");
            }
        },
    },

    Mutation: {

        async addCollection(_: void, args: { collection: NewCollectionInput }, { dataSources }): Promise<BankNoteCollection | null> {
            try {
                let { collection } = args
                console.log(`About to create collection for ${collection.collectorId}: ${collection.name}, ${collection.description}`);
                let { collectionId, name, description, collectorId } = await dataSources.collectionsAPI.createCollection(collection);
                return new BankNoteCollection(collectionId, name, description, collectorId, []);     
            } catch (err) {
                throw handleErrors(err, "addCollection")
            }
        },

        async addBankNoteToCollection(_: void, args: { data: AddBankNoteToCollection }, { dataSources }): Promise<BankNoteCollection> {
            let { data: { collectionId, collectorId, bankNoteCollectionItem: { id } } } = args;
            console.log(`Adding banknote to collection: ${collectionId}`);

            await dataSources.collectionsAPI.addItemsToCollection(collectionId, [id]);

            //TODO: the API should return the collection back (1st page or so): issue #133
            let fetchedCollection: CollectionApiResult = await dataSources.collectionsAPI.getCollectionById(collectionId);
            let bankNotes: BankNote[] = await decorator.decorateItems("en", fetchedCollection.items);
            return new BankNoteCollection(collectionId, fetchedCollection.name,
                fetchedCollection.description, collectorId, bankNotes);
        },

        async updateCollection(_: void, args: { collectionId: string, data: UpdateCollectionInput }, { dataSources: { collectionsAPI } }): Promise<BankNoteCollection> {
            let { name, description } = args.data;
            let bankNoteCollection = await collectionsAPI.updateCollection(args.collectionId, name, description);
            return new BankNoteCollection(
                bankNoteCollection.collectionId,
                bankNoteCollection.name,
                bankNoteCollection.description,
                bankNoteCollection.collectorId, []);
        },

        async deleteCollection(_: void, args: { collectionId: string }, { dataSources }): Promise<Boolean> {
            console.log(`Deleting collection ${args.collectionId}`);
            await dataSources.collectionsAPI.deleteCollection(args.collectionId);
            return true;
        },

        async removeBankNoteFromCollection(_: void, { banknoteId, collectionId }, { dataSources: { collectionsAPI } }): Promise<BankNoteCollection> {
            await collectionsAPI.deleteCollectionItem(collectionId, banknoteId);
            return decorateBanknoteCollection(collectionId, collectionsAPI);
        },

        
        // Authentication
        async loginWithEmail(_: void, { email, password }, ctx): Promise<AuthenticationResult> {
            try {
                let authResult: AuthenticationResult = await authenticationService.loginWithEmailPassword(email, password);
                console.log("Resolver: authResult", authResult);
                return authResult;
            } catch (err) {
                console.log("Authentication error in login", err);

                if (err.code === ErrorCodes.INVALID_PASSWORD_ERROR_CODE) {
                    throw new InvalidPasswordError("Invalid credentials provided");
                } else {
                    throw new AuthenticationError("Authentication error in login");
                }
            }
        },

        async loginWithGoogle(_: void, { googleAuthMaterial }, ctx): Promise<AuthenticationResult> {
            try {
                let authResult: AuthenticationResult = await authenticationService.loginWithGoogle(googleAuthMaterial);
                console.log("Resolver: authResult", authResult);
                return authResult;
            } catch (err) {
                console.log("Authentication error in login", err);
                throw new AuthenticationError("Authentication error in login");
            }
        },

        async logout(_: void, { token }) {
            return authenticationService.logout(token);
        },

        async verifyToken(_: void, { token, refresh }): Promise<AuthenticationResult> {
            try {
                const result = await authenticationService.validateToken(token, refresh);
                console.log("Result", result);
                return result;
            } catch (err) {
                console.log("Error verifying token", err);
                throw new AuthenticationError("Authentication error in login");
            }
        },

        async signUpWithEmail(_: void, args: { userInput: CreateUserCommand}): Promise<UserCreatedResult> {
            try {
                const createdUserResult = await userService.signUpWithEmail(args.userInput);
                console.log("Created User:", createdUserResult);
                return createdUserResult;
            } catch (err) {
                console.log("Error creating user", err);
                if (err instanceof InvalidValueError) {
                    throw new ValidationError("Parameters invalid creating user: " + err.message);
                } else {
                    throw new Error("Error creating user: " + err.message);
                }
            }
        },

        async verifyEmail(_: void, args: { verifyEmailInput: VerifyEmailInput}): Promise<EmailVerificationResult> {
            const verifyEmailInput = args.verifyEmailInput;

            const emailVerificationCmd = {
                actionCode: verifyEmailInput.code,
                continueUrl: verifyEmailInput.comebackUrl,
                lang: verifyEmailInput.lang
            }
            try {
                const result = await userService.verifyUserEmail(emailVerificationCmd);
                console.log("Resolver: email verified", result);
                return {
                    result: result.result,
                    email: result.email,
                    comebackUrl: result.comebackUrl,
                }
            } catch (err) {
                throw new Error("Error verifying email: " + err.message);
            }
        },

        async updateUserProfile(_: void, args: { updateUserProfileInput: UpdateUserProfileCommand}, ctx): Promise<UserProfileResult> {
            try {
                await resolverHelper.validateRequestToken(ctx.token, "updateUserProfile");    
                const updatedUserProfileResult = await userService.updateUserProfile(args.updateUserProfileInput);
                console.log("Resolver: user updated", updatedUserProfileResult);
                return updatedUserProfileResult;
            } catch (err) {
                throw handleErrors(err, "updateUserProfile");
            }
        },

        async changePassword(_: void, args: { changePasswordInput: ChangePasswordCommand}, ctx): Promise<ChangePasswordResult> {
            try {
                await resolverHelper.validateRequestToken(ctx.token, "changeUserPassword");    
                const changeUserPasswordResult = await authenticationService.changePassword(args.changePasswordInput);
                console.log("Resolver: password change", changeUserPasswordResult);
                return changeUserPasswordResult;
            } catch (err) {
                throw handleErrors(err, "changeUserPassword");
            }
        },

        async requestPasswordReset(_: void, args: { email: string}, ctx): Promise<ChangePasswordResult> {
            try {    
                const resetPasswordResult = await authenticationService.resetPasswordRequest(args.email);
                console.log("Resolver: reset password", resetPasswordResult);
                return { result: "ok"};
            } catch (err) {
                throw handleErrors(err, "resetPassword");
            }
        },

        async completePasswordReset(_: void, args: { email: string, code: string, newPassword: string}, ctx): Promise<ChangePasswordResult> {
            try {
                const cmd: CompleteResetPasswordCommand = { email: args.email, resetCode: args.code, newPassword: args.newPassword};
                const resetPasswordResult = await authenticationService.completeResetPassword(cmd);
                console.log("Resolver: complete reset password", resetPasswordResult);
                return { result: "ok"};
            } catch (err) {
                throw handleErrors(err, "completeResetPassword");
            }
        },
    }
};

const decorateBanknoteCollection =
    async (collectionId: string, collectionsAPI: CollectionsRestDatasource): Promise<BankNoteCollection> => {
        let collection: CollectionApiResult = await collectionsAPI.getItemsForCollection(collectionId);
        let bankNotes = new Array<BankNote>();
        if (collection.items) {
            console.log("Items in collection:", collection.items);
            bankNotes = await decorator.decorateItems("en", collection.items);
            console.log("Decorated banknotes:", bankNotes);
        }
        return new BankNoteCollection(collection.id, collection.name, collection.description, collection.collectorId, bankNotes);
    }

//TODO: these errors contain lots of information in stacktrace, should be cut to minimum information
// https://www.apollographql.com/docs/apollo-server/data/errors/#custom-errors
const handleErrors = (err: ApolloError, request: string): Error => {
    console.log(`Error received for ${request} request`, err);
    if (err instanceof InvalidValueError) {
        return new ValidationError(`Parameters invalid for ${request}: ${err.message}`);
    } else if (err instanceof ApolloError && err instanceof AuthenticationError) {
        console.log(`Authentication required for ${request}`);
        throw err;
    } else if (err instanceof ForbiddenError) {
        console.log(`Forbidden access during operation ${request}`);
        return err;    
    } else {
        let newErr = new GeneralError(`General error in ${request}: ${err.message}`, err["code"]);
        console.log("Err", err["code"])
        if (err["code"] === "ECONNREFUSED") {
            throw new ApolloError("Connection error with Collections API", "CONNECTION_REFUSED");
        } else {
            throw newErr;
        }
    }
}

type ErrorResult = {
    type: string
    message: string
    status: number
}

export default resolverMap;