import jwt from 'jsonwebtoken';
import { AuthenticationError } from 'apollo-server-express';

class TokenHelper {

    /**
     * Performs a basic decode of the token being passed with the request
     * 
     * @param token the JWT token
     */
    public validateToken(token: string) {
        try {
            const decodedToken: any = jwt.decode(token);
            const userId = decodedToken.user_id;
            const email = decodedToken.email;

            console.log(`Decoded token with userData: ${userId}, ${email}`);
            return { userId, email };
        } catch (err) {
            console.log("Error decoding token: ", token);
            throw new AuthenticationError("Error decoding token");
        }
    }

    /**
     * Extracts a Bearer JWT token from the request if present alongside
     * any user details from it 
     * 
     * @param req the request
     */
    public extractTokenFromRequest(req: any) {

        // Get the user token from the headers
        let token = req.headers.authorization || '';
        let user = {};

        if (token) {
            // Validate only if token present (authenticated)
            token = token.replace("Bearer", "").trim();
            user = this.validateToken(token);
        }

        // add the user and token to the context as-is, 
        // it will be checked in the relevant resolver parts
        return { user, token };
    }
}

export const tokenHelper = new TokenHelper();

