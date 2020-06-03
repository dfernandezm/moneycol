export default class EmailVerificationError extends Error {
    public readonly statusCode: number;
    constructor(message: string) {
      super(message);
      this.name = "EmailVerificationError";
      this.statusCode = 400;
    }
}
  