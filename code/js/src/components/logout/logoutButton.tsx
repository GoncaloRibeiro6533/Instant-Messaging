
import * as React from "react"

import { AuthContext } from "../auth/AuthProvider"

export function LogoutButton() {
    const {setUser} = React.useContext(AuthContext)
    function logoutHandler() {
        localStorage.removeItem('user')
        setUser(undefined)
    }
    return (
        <div>
            { /* <p>Current theme: {theme}</p> */}
            <button className="button" onClick={() => logoutHandler()}>LogOut</button>
        </div>
    )
}