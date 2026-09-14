package app.mediatracker.feature.search.client;

import app.mediatracker.config.WebClientConfig;
import app.mediatracker.feature.search.client.anime.JikanAnimeClient;
import app.mediatracker.feature.search.client.book.OpenLibraryClient;
import app.mediatracker.feature.search.client.game.RawgClient;
import app.mediatracker.feature.search.client.manga.JikanMangaClient;
import app.mediatracker.feature.search.client.movie_and_series.IMDbClient;
import app.mediatracker.feature.search.client.music.ItunesClient;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pins the headers every search client sends.
 * Jikan answers a request carrying "Accept-Encoding: gzip" with 504 (#81), and Open Library asks for a
 * User-Agent that names the application and a contact (#12). Reactor Netty adds headers below WebClient,
 * so a mocked ExchangeFunction never sees all of them. This test runs the clients with the shared builder
 * from {@link WebClientConfig} against a local HTTP server instead.
 */
class SearchClientRequestHeadersTest {

    private static final String USER_AGENT = "MediaTracker/test (+https://github.com/Elhan-Salaji/Media-Tracker)";

    private HttpServer server;
    private final AtomicReference<Headers> receivedHeaders = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            receivedHeaders.set(new Headers(exchange.getRequestHeaders()));
            byte[] body = "{ \"data\": [] }".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void animeClient_sendsExpectedHeaders() {
        // Arrange
        JikanAnimeClient client = new JikanAnimeClient(sharedBuilder(), baseUrl());

        // Act
        client.searchAnime("naruto");

        // Assert
        assertExpectedHeaders();
    }

    @Test
    void mangaClient_sendsExpectedHeaders() {
        // Arrange
        JikanMangaClient client = new JikanMangaClient(sharedBuilder(), baseUrl());

        // Act
        client.searchManga("naruto");

        // Assert
        assertExpectedHeaders();
    }

    @Test
    void bookClient_sendsExpectedHeaders() {
        // Arrange
        OpenLibraryClient client = new OpenLibraryClient(sharedBuilder(), baseUrl());

        // Act
        client.searchBook("naruto", 5);

        // Assert
        assertExpectedHeaders();
    }

    @Test
    void gameClient_sendsExpectedHeaders() {
        // Arrange
        RawgClient client = new RawgClient(sharedBuilder(), baseUrl(), "test-rawg-key");

        // Act
        client.searchGame("naruto");

        // Assert
        assertExpectedHeaders();
    }

    @Test
    void movieAndSeriesClient_sendsExpectedHeaders() {
        // Arrange
        IMDbClient client = new IMDbClient(sharedBuilder(), baseUrl());

        // Act
        client.searchMovieAndSeries("naruto");

        // Assert
        assertExpectedHeaders();
    }

    @Test
    void musicClient_sendsExpectedHeaders() {
        // Arrange
        ItunesClient client = new ItunesClient(sharedBuilder(), baseUrl());

        // Act
        client.searchTracks("naruto", 5);

        // Assert
        assertExpectedHeaders();
    }

    private WebClient.Builder sharedBuilder() {
        return new WebClientConfig().webClientBuilder(USER_AGENT);
    }

    private String baseUrl() {
        return "http://localhost:" + server.getAddress().getPort() + "/v4";
    }

    private void assertExpectedHeaders() {
        Headers headers = receivedHeaders.get();
        assertNotNull(headers, "The client sent no request");
        assertEquals(USER_AGENT, headers.getFirst("User-Agent"),
                () -> "Headers sent: " + headers.entrySet());
        assertNull(headers.getFirst("Accept-Encoding"),
                () -> "Jikan answers Accept-Encoding: gzip with 504. Headers sent: " + headers.entrySet());
    }
}
