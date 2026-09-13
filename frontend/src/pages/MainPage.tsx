import { useEffect, useState } from "react";
import Navbar from "../components/Navbar.tsx";
import Content from "../components/Content.tsx";
import Footer from "../components/Footer.tsx";
import type {MediaItem, MediaType} from "../components/types.ts";
import {useAuth} from "../service/useAuth.ts";
import {apiUrl} from "../service/api.ts";

/**
 * MainPage verwaltet den seitenweiten State und koordiniert die Suchfunktionalität.
 * Sie verbindet UI-Komponenten mit der Backend-Kommunikation.
 */

export default function MainPage() {
    // Speichert Suchergebnisse, die vom Backend zurückgegeben werden
    const [items, setItems] = useState<MediaItem[]>([]);

    // Steuert die Ladeanzeige während asynchroner Operationen
    const [loading, setLoading] = useState(true);

    // Aktueller Wert der Such-Eingabe
    const [query, setQuery] = useState("");

    // Ausgewählter Medien-Filtertyp
    const [selectedType, setSelectedType] = useState<MediaType>("anime");

    // Optionaler Backend-Limit-Parameter
    const limit = "";

    // Holt den authentifizierten Nutzer für die Anzeige in der Navbar
    const {user: loggedInUser} = useAuth();

    /**
     * Wendet seiten-spezifisches Styling an, indem Body-Klassen angepasst werden.
     * Stellt eine saubere Layout-Trennung zwischen Login- und Hauptansicht sicher.
     */
    useEffect(() => {
        document.body.classList.add("main-page");
        document.body.classList.remove("login-page");
        return () => document.body.classList.remove("main-page");
    }, []);

    /**
     * Führt eine Suchanfrage an die Backend-API aus.
     *
     * @param query - Suchbegriff des Nutzers.
     * @param type - Ausgewählter Medien-Filtertyp.
     *
     * Behandelt Ladezustand und Fehler-Fallback.
     */

    async function search(query: string, type: MediaType) {
        try {
            setLoading(true);

            // Kodiert Parameter, um fehlerhafte URLs zu vermeiden
            const url = apiUrl(`/api/search?q=${encodeURIComponent(query)}&types=${encodeURIComponent(type)}&limit=${encodeURIComponent(limit)}`);

            const response = await fetch(url, {
                credentials:"include"
            });
            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {

                throw new Error(`HTTP ${response.status}`);
            }}

            const data: MediaItem[] = await response.json();
            setItems(data);
        } catch (err) {
            console.error(err);
            setItems([]);
        } finally {
            setLoading(false);
        }
    }
    /**
     * Löst eine initiale Suche beim Mounten der Komponente aus.
     * Dient dazu, Standardergebnisse zu laden.
     */
    useEffect(() => {
        search(query, selectedType);
    }, []);

    /**
     * Startet eine neue Suche nach Validierung der Nutzereingabe.
     * Verhindert leere oder nur aus Leerzeichen bestehende Anfragen.
     */

    function handleSearch() {
        const trimmed = query.trim();
        if (!trimmed) return;

        search(trimmed, selectedType);
    }

    /**
     * Aktualisiert den ausgewählten Medientyp und
     * löst optional eine neue Suche aus, falls eine Suchanfrage existiert.
     */

    function handleTypeChange(newType: MediaType) {
        setSelectedType(newType);
        const trimmed = query.trim();
        if (!trimmed) return;
        search(trimmed, newType);
    }

    return (
        <div className="grid-container">
            <header id="header">
                <h1 className="title">Media-Tracker 3</h1>
            </header>
            <Navbar
                query={query}
                onQueryChange={setQuery}
                onSearch={handleSearch}
                username={loggedInUser?.username ?? undefined}
                profilePictureUrl={loggedInUser?.profilePictureUrl ?? undefined}
            />
            <aside id="aside">
                <h2 className="friend-title">Friends</h2>
                <button className="friend-button">Your imaginary Friend 1</button>
                <button className="friend-button">Your imaginary Friend 2</button>
                <button className="friend-button">Larry</button>
            </aside>
            <Content
                items={items}
                loading={loading}
                selectedType={selectedType}
                onTypeChange={handleTypeChange}
            />
            <Footer />
        </div>
    );
}
