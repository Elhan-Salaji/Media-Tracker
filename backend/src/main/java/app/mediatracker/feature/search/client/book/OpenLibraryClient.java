package app.mediatracker.feature.search.client.book;

import app.mediatracker.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * HTTP client for the OpenLibrary API.
 *
 * Purpose: Encapsulates HTTP communication and provides a simple method
 * to retrieve book search results as a JSON string.
 *
 * Configuration: Base URL can be overridden via "openlibrary.base-url".
 */
@Component
public class OpenLibraryClient {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a client with a predefined base URL.
     *
     * @param builder Spring-provided {@link WebClient.Builder}
     * @param baseUrl base URL of the OpenLibrary API (default: https://openlibrary.org)
     */
    public OpenLibraryClient(WebClient.Builder builder,
                             @Value("${openlibrary.base-url:https://openlibrary.org}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    /**
     * Searches for books by title, optional author, and ISBN, with a limit.
     */
    @Cacheable(CacheConfig.OPEN_LIBRARY_SEARCH)
    public String searchBook(String query, int limit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search.json")
                        .queryParam("q", query)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}