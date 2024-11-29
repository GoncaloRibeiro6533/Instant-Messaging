
import * as React from "react"

import { useAuth } from "../auth/AuthProvider"

export function LogoutButton() {
    const [user, setUser] = useAuth()
    function logoutHandler() {
        localStorage.removeItem('user')
        setUser(undefined)
    }
    return (
        <div>
            <button className="button" onClick={() => logoutHandler()}>LogOut</button>
        </div>
    )
}