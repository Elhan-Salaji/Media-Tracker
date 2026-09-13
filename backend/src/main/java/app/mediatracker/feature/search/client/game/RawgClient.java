package app.mediatracker.feature.search.client.game;

import app.mediatracker.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Minimal HTTP client for the RAWG Game Search API.
 *
 * Purpose: Provides a method to retrieve game titles based on a search term
 * as a raw JSON response.
 *
 * Configuration: Base URL can be overridden via "rawg.base-url".
 */
@Component
public class RawgClient {

    private final WebClient web;
    private final String apiKey;

    /**
     * Creates the client with a predefined base URL.
     *
     * @param builder Spring-provided {@link WebClient.Builder}
     * @param baseUrl Base URL of the RAWG API (default: https://rawg.io/api)
     * @param apiKey  API Key for authentication
     */
    public RawgClient(WebClient.Builder builder,
                      @Value("${rawg.base-url:https://rawg.io/api}") String baseUrl,
                      @Value("${rawg.api-key:}") String apiKey ) {
        this.web = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    /**
     * Searches for games on RAWG and returns the raw JSON response.
     *
     * Note: Blocks the calling thread until the response is received.
     *
     * @param query the search term
     * @return JSON response as a String
     */
    @Cacheable(CacheConfig.RAWG_SEARCH)
    public String searchGame(String query) {
        return web.get()
                .uri(u -> u.path("/games")
                        .queryParam("search", query)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block(); // keep it simple
    }
}