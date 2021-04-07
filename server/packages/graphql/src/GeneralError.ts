

export class GeneralError extends Error {
    message: string;
    errorCode: string;

    constructor(message: string, errorCode = "GENERAL_ERROR") {
        super(message.replace("GraphQL error:", ""));
    }
}
