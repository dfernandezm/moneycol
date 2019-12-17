import React, { Component, ComponentType } from "react";
import { Route, Redirect, RouteComponentProps } from "react-router-dom";
import { Dispatch } from 'redux';

export type PartialProtectedRouteProps<T> = Pick<T, Exclude<keyof T, keyof ProtectedRouteProps>>
export type ComponentWithDispatch = ComponentType

export interface ProtectedRouteProps {
    component: ComponentWithDispatch
    isAuthenticated: boolean,
    isVerifying: boolean
    exact: boolean
    path: string
}

export type TheProps = ProtectedRouteProps & PartialProtectedRouteProps<ProtectedRouteProps>

const ProtectedRoute = ({
    component: Component,
    isAuthenticated,
    isVerifying,
    ...rest
}: TheProps) => {
        //type LocationProp = { location: string }
        type ComponentProps = React.ComponentProps<typeof Component> & RouteComponentProps

        return (<Route
            {...rest}
            render={(props: ComponentProps) => {
                const {location, history, match, staticContext, ...remainderProps} = props;
                return (isVerifying ? (
                    <div />
                ) : isAuthenticated ? (
                    <Component {...remainderProps} />
                ) : (
                            <Redirect
                                to={{
                                    pathname: "/login",
                                    state: { from: location }
                                }}
                            />
                        ))
                }
            }
        />)
        };
export default ProtectedRoute;