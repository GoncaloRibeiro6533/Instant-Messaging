import * as React from "react";
import { Link, Outlet } from "react-router-dom";
import { AuthContext, AuthProvider } from "../auth/AuthProvider";

export function Home() {
  const { user } = React.useContext(AuthContext);
  return (
    <div>
      <h1>Home</h1>
        {user ? (
            <div>
              <p>Welcome, {user.user.username}!</p>
              <li><Link to="/logout">Logout</Link></li>
              </div>
        ) : (
            <div>
                <p>Welcome! Please log in.</p>
                <ol>
                    <li><Link to="/login">Login</Link></li>
                </ol>
                <Outlet />
            </div>
        )}
    </div>
  );
}