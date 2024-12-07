import * as React from "react";
import { Link, Outlet } from "react-router-dom";
import { AuthContext, AuthProvider } from "../auth/AuthProvider";
import { LogoutButton } from "../logout/logoutButton";
import { LoginButton } from "../login/loginButton";
import Logo from "../../../public/logo.png";
import { Typography } from "@mui/material";

export function Home() {
  const { user } = React.useContext(AuthContext);
  return (
      <div style={{textAlign: 'center', marginTop: '50px'}}>
          <img src={Logo} alt="Application Logo" style={{width: '400px', marginBottom: '20px'}}/>
          {user ? (
              <div>
                 <Typography variant="h5">Welcome, {user.username}!</Typography> 
                  <p></p>
                  <li><Link to="/about">About</Link></li>
                  <Link to="/register/14">Register</Link>
                  <Outlet/>
              </div>
          ) : (
              <div>
                 <Typography variant="h4">Welcome! Please log in.</Typography> 
                  <LoginButton/>
                  <Outlet/>
              </div>
          )}
      </div>
  );
}