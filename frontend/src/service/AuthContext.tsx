import { createContext, useContext, useState, useEffect, useCallback } from "react";
import type { ReactNode } from "react";
import type { User } from "../components/types.ts";
import {apiUrl} from "./api.ts";

/**
 * AuthContext / AuthProvider.
 *
 * Stellt globalen Authentifizierungs-State und Hilfsfunktionen für das Frontend bereit.
 * Zentralisiert das Session-Handling (aktueller Nutzer, Logout) und bietet einen Fetch-Wrapper,
 * der Requests nach einem Refresh einer abgelaufenen Session erneut versucht.
 *
 * Architektonische Rolle:
 * - Geteilter Anwendungs-Service, der aus jeder Komponente über `useAuth()` erreichbar ist.
 * - Verhindert duplizierte Auth-/Session-Logik über Seiten und UI-Komponenten hinweg.
 */

interface AuthContextType {
    user: (User & { userid: string }) | null;
    setUser: (user: (User & { userid: string }) | null) => void;
    logout: () => void;
    fetchWithRefresh: (url: string, options?: RequestInit) => Promise<Response>;
    isLoading: boolean;
    refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
    // Hält authentifizierte Nutzerdaten (null, wenn nicht eingeloggt)
    const [user, setUser] = useState<(User & { userid: string }) | null>(null);

    // Gibt an, ob der initiale Auth-Check noch läuft
    const [isLoading, setIsLoading] = useState(true);

    // Geteiltes Promise, um mehrere Refresh-Requests parallel zu vermeiden
    let refreshPromise: Promise<void> | null = null;

    /**
     * Wrappt `fetch`, sodass Requests Cookies enthalten und bei HTTP 401 automatisch
     * einmal nach einem Session-Refresh erneut versucht werden.
     *
     * @param url - Ziel-URL (Backend-Endpunkt)
     * @param options - Standard-Fetch-Optionen (method, headers, body, ...)
     * @returns Die finale Fetch-Response (entweder initial oder erneut versucht)
     * @throws Error wenn der Refresh fehlschlägt und die Session als abgelaufen gilt
     */

    const fetchWithRefresh = async (url: string, options: RequestInit = {}) => {
        let response = await fetch(url, {
            ...options,
            credentials: "include",
        });

        if (response.status === 401) {
            try {
                await refreshToken();
                response = await fetch(url, { ...options, credentials: "include" });
            } catch {
                // Removed unused 'exception' var
                await logout();
                throw new Error("Session expired");
            }
        }

        return response;
    };

    /**
     * Lädt den aktuell authentifizierten Nutzer aus dem Backend (`/auth/me`).
     * Setzt `user` entsprechend und beendet die initiale Ladephase.
     */

    const refreshUser = useCallback(async () => {
        try {
            const response = await fetchWithRefresh(apiUrl("/auth/me"));
            if (response.ok) {
                const data = await response.json();
                setUser(data);
            } else {
                setUser(null);
            }
        } catch {
            // Removed unused 'err' var
            setUser(null);
        } finally {
            setIsLoading(false);
        }
    }, []);

    /**
     * Führt den initialen Auth-Check einmal beim Mounten des Providers aus.
     */

    useEffect(() => {
        refreshUser();
    }, [refreshUser]);

    /**
     * Loggt den Nutzer lokal aus und informiert das Backend, die Session zu invalidieren.
     * Nach dem Logout ist `user` immer auf null gesetzt.
     */

    const logout = async () => {
        setUser(null);
        const url = apiUrl("/auth/logout");
        try {
            await fetch(url, {
                method: "POST",
                credentials: "include"
            });
        } catch (e) {
            console.error("Logout failed", e);
        }
    };

    /**
     * Fordert eine aktualisierte Session / ein aktualisiertes Token beim Backend an (`/auth/refresh`).
     * Verwendet ein geteiltes Promise, um parallele Refresh-Aufrufe zu verhindern.
     *
     * @returns Ein Promise, das auflöst, wenn der Refresh erfolgreich war
     * @throws Error wenn der Refresh fehlschlägt
     */

    async function refreshToken() {
        if (refreshPromise) return refreshPromise;

        refreshPromise = (async () => {
            const res = await fetch(apiUrl("/auth/refresh"), {
                method: 'POST',
                credentials: 'include'
            });

            if (!res.ok) {
                await logout();
                throw new Error('Refresh failed');
            }
            refreshPromise = null;
        })();

        return refreshPromise;
    }

    return (
        <AuthContext.Provider value={{ user, setUser, logout, isLoading, fetchWithRefresh, refreshUser }}>
            {children}
        </AuthContext.Provider>
    );
}

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
