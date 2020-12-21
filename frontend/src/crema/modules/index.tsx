import React from 'react';
import {Redirect} from 'react-router-dom';
import NavItemProps from './routesConfig';
import {createRoutes} from '../@crema/utility/Utils';
// import {samplePagesConfig} from './sample';
// import {errorPagesConfigs} from './errorPages';
// import {authRouteConfig} from './auth';
import {initialUrl} from '../shared/constants/AppConst';

const routeConfigs: typeof NavItemProps[] = [
  // ...samplePagesConfig,
  // ...errorPagesConfigs,
  // ...authRouteConfig,
];

const routes = [
  ...createRoutes(routeConfigs),
  {
    path: '/',
    exact: true,
    component: () => <Redirect to={initialUrl} />,
  },
  {
    component: () => <Redirect to='/error-pages/error-404' />,
  },
];

export default routes;
