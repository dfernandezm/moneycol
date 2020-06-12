export default class UserInInvalidStateError extends Error {
    public readonly statusCode: number;
    constructor(message: string) {
      super(message);
      this.name = "UserInInvalidStateError";
      this.statusCode = 404;
    }
}
  