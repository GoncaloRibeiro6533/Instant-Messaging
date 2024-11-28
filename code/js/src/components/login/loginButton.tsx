import * as React from "react"
import { useNavigate } from "react-router-dom"

export function LoginButton() {
    const navigate = useNavigate()
    return (
        <div>
            <button className="button" onClick={() => navigate("/login")}>LogIn</button>
        </div>
    )
}