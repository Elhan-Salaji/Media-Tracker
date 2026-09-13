import { createContext } from "react";
import type { User } from "../components/types.ts";

/**
 * Form des Authentifizierungs-Contexts, den `AuthProvider` bereitstellt.
 *
 * Context und Typ liegen in einer eigenen Datei, damit `AuthContext.tsx` nur Komponenten
 * exportiert. Sonst kann React Fast Refresh die Datei beim Bearbeiten nicht neu laden.
 */
export interface AuthContextType {
    user: (User & { userid: string }) | null;
    setUser: (user: (User & { userid: string }) | null) => void;
    logout: () => Promise<void>;
    fetchWithRefresh: (url: string, options?: RequestInit) => Promise<Response>;
    isLoading: boolean;
    refreshUser: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
