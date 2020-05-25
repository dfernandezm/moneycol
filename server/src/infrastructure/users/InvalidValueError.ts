export default class InvalidValueError extends Error {
    public readonly statusCode: number;
    constructor(message: string) {
      super(message);
      this.name = "InvalidValueError";
      this.statusCode = 400;
    }
}
  