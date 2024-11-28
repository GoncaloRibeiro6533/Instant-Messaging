
import * as React from "react"

import { AuthContext } from "../auth/AuthProvider"
import { useAuth } from "../auth/AuthProvider"

export function LogoutButton() {
    const [user, setUser] = useAuth()
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