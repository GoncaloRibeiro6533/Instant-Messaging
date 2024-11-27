import * as React from 'react';
import { useLogin } from './useLogin';
import { Navigate, useLocation } from 'react-router-dom';

export function Login() {
    const [state, handlers] = useLogin()
    const location = useLocation();
    if (state.name === 'redirecting') {
        return <Navigate to={location.state?.source || '/'} replace={true} />;
    }
    return (
        <div>
            <h1>Login</h1>
            <form onSubmit={handlers.onSubmit}>
                <fieldset disabled={state.name !== 'editing'}>
                    <div>
                        <label htmlFor="username">Username</label>
                        <input id="username" type="text" name="username" value={state.username} onChange={handlers.onChange} />
                    </div>
                    <div>
                        <label htmlFor="password">Password</label>
                        <input id="password" type="password" name="password" value={state.password} onChange={handlers.onChange} />
                    </div>
                    <div>
                        <button type="submit">Login</button>
                    </div>
            </fieldset>
      {state.name === 'editing' && state.error}
    </form>
        </div>
    )
}