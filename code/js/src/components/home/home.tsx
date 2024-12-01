import * as React from "react";
import { Link, Outlet } from "react-router-dom";
import { AuthContext, AuthProvider } from "../auth/AuthProvider";
import { LogoutButton } from "../logout/logoutButton";
import { LoginButton } from "../login/loginButton";
import Logo from "../../../public/logo.png";

export function Home() {
  const { user } = React.useContext(AuthContext);
  return (
      <div style={{textAlign: 'center', marginTop: '50px'}}>
          <img src={Logo} alt="Application Logo" style={{width: '400px', marginBottom: '20px'}}/>
          {user ? (
              <div>
                  <p>Welcome, {user.user.username}!</p>
                  <li><Link to="/about">About</Link></li>
                  <Link to="/register/14">Register</Link>
                  <Outlet/>
              </div>
          ) : (
              <div>
                  <p>Welcome! Please log in.</p>
                  <LoginButton/>
                  <Outlet/>
              </div>
          )}
      </div>
  );
}