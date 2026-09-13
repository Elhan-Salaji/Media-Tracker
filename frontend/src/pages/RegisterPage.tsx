import {type FormEvent, useEffect, useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import {apiUrl} from "../service/api.ts";

/**
 * RegisterPage-Komponente.
 *
 * Verantwortlichkeit:
 * - Stellt das Registrierungsformular für Nutzer bereit.
 * - Führt einfache client-seitige Validierung durch.
 * - Sendet die Registrierungsanfrage an das Backend.
 * - Leitet nach erfolgreicher Registrierung zur Login-Seite weiter.
 *
 * Architektonische Rolle:
 * - Präsentations- + Authentifizierungs-Interaktionsschicht.
 * - Delegiert Persistenz und Nutzererstellung an das Backend.
 */

export default function RegisterPage() {
    // Router-Navigations-Hook
    const navigate = useNavigate()

    // Speichert Fehlermeldungen aus Validierung oder Backend
    const [error, setError] = useState<string | null>(null);

    /**
     * Wendet das Login-Layout auch auf die Registrierungsseite an,
     * um ein konsistentes Authentifizierungs-UI sicherzustellen.
     */
    useEffect(() => {
        document.body.classList.add("login-page");
        return () => {document.body.classList.remove("login-page");
        };
    }, []);

    /**
     * Behandelt das Submit-Event des Formulars
     * und startet den Registrierungsprozess.
     */
    async function handleSubmit(event:FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setError(null)

        const formData = new FormData(event.currentTarget);
        const username = formData.get("username") as string;
        const password = formData.get("password") as string;
        const passwordrep = formData.get("passwordrep") as string;

        try {
            await register(username, password, passwordrep)
            navigate("/login")
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
     * Sendet die Registrierungsanfrage an den Authentifizierungs-Endpunkt im Backend.
     *
     * Führt vor dem Senden eine client-seitige Passwortvalidierung durch.
     *
     * @throws Error wenn:
     * - Passwörter nicht übereinstimmen
     * - Benutzername bereits vergeben ist (409)
     * - Server einen generischen Fehler zurückgibt
     */
    async function register(username: string, password: string, passwordRep: string) {
        if(!validatePassword(password, passwordRep)) {
            throw new Error("Passwords don't match.")
        }

        const url = apiUrl("/auth/register")
        const response = await fetch(url, {
            method: "POST",
            credentials: "include",
            headers: {
                'content-type': 'application/json'
            },
            body: JSON.stringify({username, password, passwordRep})
        })

        if (!response.ok) {
            if(response.status === 409) {
                throw new Error("Username already taken.")
            }
            throw new Error("An error occurred. Please try again later.");
        }
    }

    /**
     * Einfache client-seitige Validierung, um sicherzustellen,
     * dass die Passwort-Bestätigung übereinstimmt.
     */
    function validatePassword(password: string, passwordrep: string) {
        return password === passwordrep;
    }

    return (
        <div className="wrapper">
            <form onSubmit={handleSubmit}>
                <h1>Register</h1>
                <div className="input-box">
                    <input name="username" type="text" placeholder="Username" required/>
                </div>
                <div className="input-box">
                    <input name="password" type="password" placeholder="Password" required/>
                </div>
                <div className="input-box">
                    <input name="passwordrep" type="password" placeholder="Repeat Password" required/>
                </div>
                {/* Zeigt Validierungs- oder Backend-Fehlermeldungen an */}
                {error && <div className="error-message">{error}</div>}
                <button type="submit" className="btn">Register</button>
                <div className="register-link">
                    <p>Have an account already? <Link to="/login">Login</Link></p>
                </div>
            </form>
        </div>
    )
}
