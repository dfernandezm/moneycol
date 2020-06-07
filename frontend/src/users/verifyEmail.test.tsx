import React from 'react'
import { render, waitFor, screen } from '@testing-library/react';
import '@testing-library/jest-dom/extend-expect'
import VerifyEmail from "./verifyEmail"

import { createMemoryHistory, createLocation } from 'history';
import { match } from 'react-router';

const history = createMemoryHistory();
const path = `/users/verifyEmail`;

const matchMock: match<{  }> = {
    isExact: false,
    path,
    url: path,
    params: { }
};

const location = createLocation(matchMock.url);
location.search = "oobCode=theCode&continueUrl=https://anotherUrl&lang=en";
describe ("verify email", () => {
  test.skip('verifies correctly', async () => {
    render(<VerifyEmail 
              history={history}
              location={location}
              match={matchMock} />);
  
    
    await waitFor(() => screen.getByRole('button'));
    expect(screen.getByRole('information')).toHaveTextContent('Verify email');
    // TODO: do a test that mocks the Apollo mutation (see their docs) and testing-library ones: 
    // https://dev.to/slawomirkolodziej/how-to-test-components-using-apollo-with-react-testing-library-om7
  })
});
