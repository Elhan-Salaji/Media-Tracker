import { useContext } from "react";
import { AuthContext } from "./AuthContextValue.ts";

/**
 * React-Hook, um Authentifizierungs-State und Helper zu nutzen.
 *
 * @throws Error falls außerhalb eines AuthProviders verwendet
 */
export function useAuth() {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
}
