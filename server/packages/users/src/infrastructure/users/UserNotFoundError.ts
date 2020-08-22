export default class UserNotFoundError extends Error {
    public readonly statusCode: number;
    constructor(message: string) {
      super(message);
      this.name = "UserNotFoundError";
      this.statusCode = 404;
    }
}
  