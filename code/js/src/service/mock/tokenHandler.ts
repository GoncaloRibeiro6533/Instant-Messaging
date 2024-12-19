


export function tokenHandler() {
    return {
        getToken: () => {
            return localStorage.getItem("token")
        },
        setToken: (token: string) => {
            localStorage.setItem("token", token)
            }
        };
    }