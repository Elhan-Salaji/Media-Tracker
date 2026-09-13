package app.mediatracker.feature.search.client.anime;

import app.mediatracker.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Lightweight HTTP client for the Jikan API (MyAnimeList proxy).
 *
 * Purpose: Encapsulates HTTP communication and provides a simple method
 * to retrieve anime search results as a JSON string.
 *
 * Configuration: Base URL can be overridden via the property "jikan.base-url".
 */
@Component
public class JikanAnimeClient {

    private final WebClient web;

    /**
     * Creates a client with a predefined base URL.
     *
     * @param builder Spring-provided {@link WebClient.Builder}
     * @param baseUrl base URL of the Jikan API (default: https://api.jikan.moe/v4)
     */
    public JikanAnimeClient(WebClient.Builder builder,
                            @Value("${jikan.base-url:https://api.jikan.moe/v4}") String baseUrl) {
        this.web = builder.baseUrl(baseUrl).build();
    }

    /**
     * Searches for anime on Jikan and returns the raw JSON response.
     *
     * Note: Blocks the calling thread until the response is received (simplified usage).
     *
     * @param query the search term
     * @return JSON response as a String
     */
    @Cacheable(CacheConfig.JIKAN_ANIME_SEARCH)
    public String searchAnime(String query) {
        return web.get()
                .uri(u -> u.path("/anime").queryParam("q", query).build())
                .retrieve()
                .bodyToMono(String.class)
                .block(); // keep it simple
    }
}