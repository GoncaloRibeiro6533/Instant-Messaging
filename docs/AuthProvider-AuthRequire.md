# Authentication Management: AuthContext

The `AuthContext` is a centralized solution for managing user authentication in your React application. It provides seamless integration with React's `Context API`, offering a reactive way to store user authentication state globally. Additionally, it persists the user session using `localStorage`, ensuring a smooth user experience across browser reloads.

---

## Overview

The `AuthContext` module consists of three key components:

1. **AuthContext**: A React Context for managing authentication state.
2. **AuthProvider**: A provider component to wrap your application or sections of it that need access to authentication.
3. **useAuth Hook**: A custom hook to simplify accessing and modifying the authentication state.

---

## Components

### **1. AuthContext**

The `AuthContext` is a `React Context` object that stores the currently authenticated user and provides a method to update the user state.

#### Type Definition
```typescript
type AuthContextType = {
    user: User | undefined, // The authenticated user or undefined if no user is logged in
    setUser: (user: User | undefined) => void, // Function to update the user state
};
````

# Authentication Requirement: `AuthRequire` Component

The `AuthRequire` component ia a component that enforces authentication for accessing certain parts of your application. It acts as a guard, ensuring only authenticated users can access specific routes or features.

---

## Overview

The `AuthRequire` component checks if a user is logged in (authenticated). If the user is not authenticated, it redirects them to the login page while preserving the original requested path. This allows for a smooth redirection back to the intended destination after successful login.

---

## Component Details

### **Props**
- `children`: React nodes representing the components or pages to render if the user is authenticated.

---

### **Functionality**

1. **Authentication Check**: 
   - The component uses the `AuthContext` to check if a user is currently logged in (`user` is not `undefined`).

2. **Redirect to Login**: 
   - If the `user` is not authenticated, it redirects to the `/login` route using the `Navigate` component from `react-router-dom`.
   - The `state` object includes the `source` property, which stores the current pathname (using `useLocation`). This allows the app to navigate back to the original page after login.

3. **Render Authenticated Content**:
   - If the `user` is authenticated, the component renders the `children` passed as props.

---

### Code Example

```tsx
import * as React from "react";
import { AuthContext } from "./AuthProvider";
import { Navigate, useLocation } from "react-router-dom";

export function AuthRequire({ children }: { children: React.ReactNode }) {
    const { user } = React.useContext(AuthContext); // Access authentication state
    const location = useLocation(); // Get current location for redirection

    // If user is authenticated, render children
    if (user) { 
        return <>{children}</>; 
    } else {
        // If not authenticated, redirect to login
        return <Navigate to={"/login"} state={{ source: location.pathname }}></Navigate>;
    }
}
