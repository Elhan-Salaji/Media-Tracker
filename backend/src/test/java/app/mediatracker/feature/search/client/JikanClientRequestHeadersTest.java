package app.mediatracker.feature.search.client;

import app.mediatracker.config.WebClientConfig;
import app.mediatracker.feature.search.client.anime.JikanAnimeClient;
import app.mediatracker.feature.search.client.manga.JikanMangaClient;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pins the headers the Jikan clients send.
 * Jikan answers a request carrying "Accept-Encoding: gzip" with 504 (#81). Reactor Netty adds that
 * header below WebClient, so a mocked ExchangeFunction never sees it. This test runs the clients with
 * the shared builder from {@link WebClientConfig} against a local HTTP server instead.
 */
class JikanClientRequestHeadersTest {

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
    void animeClient_sendsNoAcceptEncoding() {
        // Arrange
        JikanAnimeClient client = new JikanAnimeClient(new WebClientConfig().webClientBuilder(), baseUrl());

        // Act
        client.searchAnime("naruto");

        // Assert
        assertNoAcceptEncoding();
    }

    @Test
    void mangaClient_sendsNoAcceptEncoding() {
        // Arrange
        JikanMangaClient client = new JikanMangaClient(new WebClientConfig().webClientBuilder(), baseUrl());

        // Act
        client.searchManga("naruto");

        // Assert
        assertNoAcceptEncoding();
    }

    private String baseUrl() {
        return "http://localhost:" + server.getAddress().getPort() + "/v4";
    }

    private void assertNoAcceptEncoding() {
        Headers headers = receivedHeaders.get();
        assertNotNull(headers, "The client sent no request");
        assertNull(headers.getFirst("Accept-Encoding"),
                () -> "Jikan answers Accept-Encoding: gzip with 504. Headers sent: " + headers.entrySet());
    }
}
