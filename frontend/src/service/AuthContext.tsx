import { useState, useEffect, useCallback, useRef } from "react";
import type { ReactNode } from "react";
import type { User } from "../components/types.ts";
import {apiUrl} from "./api.ts";
import {AuthContext} from "./AuthContextValue.ts";

/**
 * AuthProvider.
 *
 * Stellt globalen Authentifizierungs-State und Hilfsfunktionen für das Frontend bereit.
 * Zentralisiert das Session-Handling (aktueller Nutzer, Logout) und bietet einen Fetch-Wrapper,
 * der Requests nach einem Refresh einer abgelaufenen Session erneut versucht.
 *
 * Architektonische Rolle:
 * - Geteilter Anwendungs-Service, der aus jeder Komponente über `useAuth()` erreichbar ist.
 * - Verhindert duplizierte Auth-/Session-Logik über Seiten und UI-Komponenten hinweg.
 *
 * Die Funktionen im Context sind mit `useCallback` stabil. Komponenten können sie deshalb
 * als Abhängigkeit in `useEffect` angeben, ohne bei jedem Render neu zu laden.
 */

export function AuthProvider({ children }: { children: ReactNode }) {
    // Hält authentifizierte Nutzerdaten (null, wenn nicht eingeloggt)
    const [user, setUser] = useState<(User & { userid: string }) | null>(null);

    // Gibt an, ob der initiale Auth-Check noch läuft
    const [isLoading, setIsLoading] = useState(true);

    // Geteiltes Promise, um mehrere Refresh-Requests parallel zu vermeiden.
    // Als Ref überlebt es Re-Renders; eine lokale Variable begänne bei jedem Render neu.
    const refreshPromise = useRef<Promise<void> | null>(null);

    /**
     * Loggt den Nutzer lokal aus und informiert das Backend, die Session zu invalidieren.
     * Nach dem Logout ist `user` immer auf null gesetzt.
     */
    const logout = useCallback(async () => {
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
    }, []);

    /**
     * Fordert eine aktualisierte Session / ein aktualisiertes Token beim Backend an (`/auth/refresh`).
     * Verwendet ein geteiltes Promise, um parallele Refresh-Aufrufe zu verhindern.
     *
     * @returns Ein Promise, das auflöst, wenn der Refresh erfolgreich war
     * @throws Error wenn der Refresh fehlschlägt
     */
    const refreshToken = useCallback(() => {
        if (refreshPromise.current) return refreshPromise.current;

        refreshPromise.current = (async () => {
            try {
                const res = await fetch(apiUrl("/auth/refresh"), {
                    method: 'POST',
                    credentials: 'include'
                });

                if (!res.ok) {
                    await logout();
                    throw new Error('Refresh failed');
                }
            } finally {
                // Auch nach einem Fehlschlag darf der nächste Aufruf einen neuen Refresh starten
                refreshPromise.current = null;
            }
        })();

        return refreshPromise.current;
    }, [logout]);

    /**
     * Wrappt `fetch`, sodass Requests Cookies enthalten und bei HTTP 401 automatisch
     * einmal nach einem Session-Refresh erneut versucht werden.
     *
     * @param url - Ziel-URL (Backend-Endpunkt)
     * @param options - Standard-Fetch-Optionen (method, headers, body, ...)
     * @returns Die finale Fetch-Response (entweder initial oder erneut versucht)
     * @throws Error wenn der Refresh fehlschlägt und die Session als abgelaufen gilt
     */
    const fetchWithRefresh = useCallback(async (url: string, options: RequestInit = {}) => {
        let response = await fetch(url, {
            ...options,
            credentials: "include",
        });

        if (response.status === 401) {
            try {
                await refreshToken();
                response = await fetch(url, { ...options, credentials: "include" });
            } catch {
                await logout();
                throw new Error("Session expired");
            }
        }

        return response;
    }, [refreshToken, logout]);

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
            setUser(null);
        } finally {
            setIsLoading(false);
        }
    }, [fetchWithRefresh]);

    /**
     * Führt den initialen Auth-Check einmal beim Mounten des Providers aus.
     */
    useEffect(() => {
        refreshUser();
    }, [refreshUser]);

    return (
        <AuthContext.Provider value={{ user, setUser, logout, isLoading, fetchWithRefresh, refreshUser }}>
            {children}
        </AuthContext.Provider>
    );
}
