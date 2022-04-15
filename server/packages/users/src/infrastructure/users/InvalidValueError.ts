export default class InvalidValueError extends Error {
    public readonly statusCode: number;
    public readonly context?: object;

    constructor(message: string, context?: object) {
      super(message);
      this.name = "InvalidValueError";
      this.statusCode = 400;
      this.context = context; // TODO: include in message
    }
}