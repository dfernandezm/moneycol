// How to protect routes with login using redirects (2nd answer)
// https://stackoverflow.com/questions/31084779/how-to-restrict-access-to-routes-in-react-router
// https://serverless-stack.com/chapters/setup-secure-pages.html

import React from 'react';
// don't use destructuring { Name } if only one export is done

export default class Login extends React.Component {
  
    render() {
      return (
        <div className="login-page">
          <h3> Login Page </h3>
        </div>
      );
    }
  }
  
