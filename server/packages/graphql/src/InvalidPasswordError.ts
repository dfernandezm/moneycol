import { AuthenticationError } from 'apollo-server-express';

export class InvalidPasswordError extends AuthenticationError {
    constructor(message: string) {
        super(message.replace("GraphQL error:", ""));
    }
}
