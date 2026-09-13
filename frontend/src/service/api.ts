/**
 * Zentrale Adresse des Backends.
 *
 * Vite setzt `VITE_API_BASE_URL` beim Build ein. Den Wert liefert die Umgebung oder
 * `frontend/.env`; ohne beides gilt der Default aus `vite.config.ts`.
 */
export const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL.replace(/\/+$/, "");

/**
 * Baut aus einem Pfad wie `/api/search` die vollständige Backend-URL.
 *
 * @param path - Pfad relativ zur Backend-Wurzel, mit oder ohne führenden Slash
 * @returns Die absolute URL für `fetch`
 */
export function apiUrl(path: string): string {
    return `${API_BASE_URL}${path.startsWith("/") ? path : `/${path}`}`;
}
