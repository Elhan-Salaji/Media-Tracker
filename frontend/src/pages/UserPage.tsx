import { useEffect, useState, } from "react";
import { useParams} from "react-router-dom";
import Navbar from "../components/Navbar.tsx";
import UserPageContent from "../components/UserPageContent.tsx";
import Footer from "../components/Footer.tsx";
import type { UserMediaSortOption, UserMediaStatus, UserPageResponse } from "../components/types.ts";
import defaultAvatar from "../assets/profile-picture.png";
import {useAuth} from "../service/useAuth.ts";
import {apiUrl} from "../service/api.ts";

/**
 * UserPage-Komponente.
 *
 * Verantwortlichkeit:
 * - Lädt und zeigt ein öffentliches Nutzerprofil sowie dessen Medienbibliothek an.
 * - Verwaltet seitenweiten State für Laden, Filter und Suchanfrage.
 * - Koordiniert Kind-Komponenten (Navbar, UserPageContent, Footer).
 *
 * Architektonische Rolle:
 * - Seiten-/Orchestrierungs-Schicht.
 * - Nutzt die AuthContext-Abstraktion (`fetchWithRefresh`) für authentifizierte Requests.
 * - Delegiert Rendering und client-seitige Filterung an Kind-Komponenten.
 */

export default function UserPage() {
    // Liest den Ziel-Benutzernamen aus der Route (z.B. /user/:username)
    const {username} = useParams<{ username: string }>();
    // Profildaten des besuchten Nutzers
    const [user, setUser] = useState<UserPageResponse["user"] | null>(null);
    // Bibliotheks-Einträge des besuchten Nutzers
    const [mediaList, setMediaList] = useState<UserPageResponse["userMediaList"]>([]);
    // Steuert den Seiten-Ladezustand während Profildaten geladen werden
    const [loading, setLoading] = useState(true);
    // Suchanfrage für client-seitige Filterung in UserPageContent
    const [query, setQuery] = useState("");
    // Ausgewählter Medientyp-Filter (client-seitig)
    const [selectedSortType, setSelectedSortType] = useState<UserMediaSortOption>("all");
    // Ausgewählter Status-Filter (client-seitig)
    const [selectedMediaStatus, setSelectedMediaStatus] = useState<UserMediaStatus | "ALL">("ALL");
    // Fallback-Avatarbild, falls profilePictureUrl fehlt oder ungültig ist
    const profileSrc = user?.profilePictureUrl?.trim() ? user.profilePictureUrl : defaultAvatar;
    // Eingeloggter Nutzer wird für die Anzeige in der Navbar verwendet (Profil-Button)
    const { user: loggedInUser, fetchWithRefresh } = useAuth();


    /**
     * Lädt Nutzerprofil-Daten (user + media list) basierend auf dem Routenparameter.
     * Nutzt den fetch-Wrapper aus dem AuthContext, um konsistentes Session-Handling sicherzustellen.
     */
    useEffect(() => {
        if (!username) return;
        setLoading(true);
        fetchWithRefresh(apiUrl(`/api/user/${encodeURIComponent(username)}`))
            .then(res => res.json())
            .then((data: UserPageResponse) => {
                //console.log("Fetched data: ", data)
                setUser(data.user);
                setMediaList(data.userMediaList)
            })
            .catch(err => {
                console.error(err);
                setUser(null);
                setMediaList([]);
            })
            .finally(() => setLoading(false));
    }, [username]);

    /**
     * Aktualisiert die Medientyp-Filterauswahl für die client-seitige Filterung.
     */
    function handleSortTypeChange(newOption: UserMediaSortOption): void {
        setSelectedSortType(newOption);
    }

    /**
     * Aktualisiert die Status-Filterauswahl für die client-seitige Filterung.
     */
    function handleMediaStatusChange(newStatus: UserMediaStatus | "ALL"): void {
        setSelectedMediaStatus(newStatus);
    }


    return (
        <div className="UserPage">
            {loading ? (
                // Ladezustand, während Profildaten geladen werden
                <div>Loading...</div>
            ) : user ? (
                <div className="grid-container">
                    <header id="header">
                        <div id="profile-pic-wrapper">
                            <img id="profile-picture" src={profileSrc} alt={`${user.username}'s profile`}/>
                        </div>
                        <h1 className="title">{user.username}'s Media List</h1>
                    </header>

                    <nav id="navbar">
                        <Navbar
                            query={query}
                            onQueryChange={setQuery}
                            // Suche wird client-seitig in UserPageContent angewendet
                            onSearch={() => {
                            }}
                            username={loggedInUser?.username}
                            profilePictureUrl={loggedInUser?.profilePictureUrl ?? null}
                        />
                    </nav>

                    <aside id="aside">
                        {/* friends (placeholder)*/}
                    </aside>

                    <main id="content">
                        <UserPageContent
                            items={mediaList}
                            searchQuery={query}
                            selectedSortType={selectedSortType}
                            selectedMediaStatus={selectedMediaStatus}
                            onSortTypeChange={handleSortTypeChange}
                            onMediaStatusChange={handleMediaStatusChange}
                        />
                    </main>

                    <footer id="footer">
                        <Footer/>
                    </footer>
                </div>
            ) : (
                // Fallback-Ansicht, wenn das angefragte Nutzerprofil nicht existiert
                <div>User not found.</div>
            )}
        </div>
    );
}
