import React, { ComponentType } from "react";
import { Route, Redirect, RouteComponentProps } from "react-router-dom";

//TODO: write a documentation about this: how PartialProtectedRouteProps works and 
// how ...rest is separated at least 
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

        type ComponentProps = React.ComponentProps<typeof Component> & RouteComponentProps

        return (<Route
            {...rest}
            render={(props: ComponentProps) => {
                const {location, history, match, staticContext, ...remainderProps} = props;
                return (isVerifying ? (
                    <div>
                        Verifying...
                    </div>
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