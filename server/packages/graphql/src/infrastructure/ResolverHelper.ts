import { authenticationService, AuthenticationService } from './authentication/AuthenticationService';
import { AuthenticationError } from 'apollo-server-express';

class ResolverHelper {

    private readonly authService: AuthenticationService;

    constructor(authService: AuthenticationService) {
        this.authService = authService;
    }

    public async validateRequestToken(token: string, request: string) {
        if (!token) {
            throw new AuthenticationError("No token present for request: " + request);
        } else {
            try {
                // It has to be a fresh token, the client must have generated it recently
                const result = await this.authService.validateToken(token, false);
                console.log("Validated token for request " + request, result);
            } catch (err) {
                console.log("Invalid token for request " + request, err);
                throw new AuthenticationError(`Invalid token for request: ${request}`);
            }
        }
    }
}

export const resolverHelper = new ResolverHelper(authenticationService);