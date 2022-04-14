import { handleErrors }  from '../src/resolverMap';
import { ApolloError } from 'apollo-server-express';
import { ErrorCodes } from '../src/errorCodes';

describe('resolverMap', () => {

    it('converts weak password error to ApolloError', async () => {
      const err: unknown = { code: ErrorCodes.WEAK_PASSWORD_ERROR_CODE, message: "" }
      const requestName = "anyRequest";
      const returnedError = handleErrors(err, requestName);
      expect(returnedError).toBeInstanceOf(ApolloError)
    })
})
