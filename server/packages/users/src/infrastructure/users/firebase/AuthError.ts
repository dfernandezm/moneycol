export default class AuthError extends Error {
    
    public readonly code: string;
    
    constructor(code: string, message: string) {
      super(message);
      this.name = "AuthError";
      this.code = code;
    }
}
  