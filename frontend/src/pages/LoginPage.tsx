import { type FormEvent, useEffect, useState } from "react";
import {Link, useNavigate} from "react-router-dom";
import {useAuth} from "../service/useAuth.ts";
import {apiUrl} from "../service/api.ts";

/**
 * LoginPage-Komponente.
 *
 * Verantwortlichkeit:
 * - Stellt das Authentifizierungsformular für den Login bereit.
 * - Sendet Credentials an das Backend und verarbeitet die Login-Antwort.
 * - Aktualisiert den globalen Authentifizierungsstatus über den AuthContext.
 *
 * Architektonische Rolle:
 * - Präsentations- + Authentifizierungs-Interaktionsschicht.
 * - Delegiert Session-Management an den AuthContext.
 * - Navigiert nach erfolgreichem Login.
 */

export default function LoginPage() {
    // Router-Navigations-Hook
    const navigate = useNavigate();
    // Hält Fehlermeldungen aus dem Login-Versuch
    const [error, setError] = useState<string | null>(null);

    /**
     * Wendet login-spezifisches Body-Styling an.
     * Stellt Layout-Trennung zwischen Login und der Hauptansicht sicher.
     */
    useEffect(() => {
        document.body.classList.add("login-page");
        document.body.classList.remove("main-page");
        return () => document.body.classList.remove("login-page");
    }, []);

    // Holt die refreshUser-Funktion, um den globalen Auth-State zu aktualisieren
    const {refreshUser} = useAuth();

    /**
     * Behandelt das Submit-Event des Formulars.
     * Liest Formulardaten aus und triggert den Login-Prozess.
     */
    async function handleSubmit(event:FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setError(null)

        const formData = new FormData(event.currentTarget);
        const username = formData.get("username") as string;
        const password = formData.get("password") as string;
        const isRememberMe = formData.get("remember-me") === "on";

        try {
            await login(username, password, isRememberMe)
            navigate("/main")
        } catch (err) {
            // FIX: Korrekte Fehlerprüfung statt ts-ignore
            if (err instanceof Error) {
                setError(err.message)
            } else {
                setError("An unknown error occurred")
            }
        }
    }

    /**
     * Sendet eine Login-Anfrage an den Authentifizierungs-Endpunkt im Backend.
     *
     * @param username - Login-Identifikator des Nutzers
     * @param password - Passwort des Nutzers
     * @param rememberMe - Gibt an, ob ein persistenter Login gewünscht ist
     *
     * Bei Erfolg:
     * - Aktualisiert den authentifizierten Nutzer-State
     * - Gibt true zurück
     *
     * Wirft einen Error bei:
     * - Ungültigen Credentials (401)
     * - Generischen Serverfehlern
     */

    async function login(username: string, password: string, rememberMe: boolean) {
        const url = apiUrl("/auth/login")

        const response = await fetch(url, {
            method: "POST",
            credentials: "include",
            headers: {
                'content-type': 'application/json'
            },
            body: JSON.stringify({username, password, rememberMe})
        });

        if (!response.ok) {
            if(response.status === 401) {
                throw new Error("Incorrect username or password")
            }

            throw new Error("An error occurred. Please try again later.");
        }

        // Synchronisiert den globalen Auth-State nach erfolgreichem Login
        await refreshUser()
        return true
    }

    return (
        <div className="wrapper">
            <form onSubmit={handleSubmit}>
                <h1>Login</h1>
                <div className="input-box">
                    <input name="username" type="text" placeholder="Username" required/>
                </div>
                <div className="input-box">
                    <input name="password" type="password" placeholder="Password" required/>
                </div>
                {/* Zeigt Backend-Validierungsfehler an */}
                {error && <div className="error-message">{error}</div>}
                <label>
                    <input type="checkbox" id="remember-me" name="remember-me" />
                    Remember Me
                </label>
                <button type="submit" className="btn">Login</button>
                <div className="register-link">
                    <p>Don't have an account? <Link to={"/register"}>Register</Link></p>
                </div>
            </form>
        </div>
    );
}
