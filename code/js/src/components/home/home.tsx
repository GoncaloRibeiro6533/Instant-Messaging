import * as React from "react";
import { Link, Outlet } from "react-router-dom";
import { AuthContext, AuthProvider } from "../auth/AuthProvider";
import { LogoutButton } from "../logout/logoutButton";
import { LoginButton } from "../login/loginButton";

export function Home() {
  const { user } = React.useContext(AuthContext);
  return (
    <div>
      <h1>Home</h1>
        {user ? (
            <div>
              <p>Welcome, {user.user.username}!</p>
              <LogoutButton />
              <li><Link to="/about">About</Link></li>
              <li><Link to="/channel/1">Channel 1</Link></li>
              <Outlet />
              </div>
        ) : (
            <div>
                <p>Welcome! Please log in.</p>
                <ol>
                    <LoginButton />
                    <li><Link to="/about">About</Link></li>
                </ol>
                <Outlet />
            </div>
        )}
    </div>
  );
}