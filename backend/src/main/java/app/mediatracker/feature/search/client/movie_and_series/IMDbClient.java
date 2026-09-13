package app.mediatracker.feature.search.client.movie_and_series;

import app.mediatracker.config.CacheConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Lightweight HTTP client for the IMDb API.
 *
 * Purpose: Encapsulates HTTP communication and provides a simple method
 * to retrieve movie search results as a JSON string.
 *
 * Configuration: Base URL can be overridden via "imdb.base-url".
 */
@Component
public class IMDbClient {

    private final WebClient web;

    /**
     * Creates a client with a predefined base URL.
     *
     * @param builder Spring-provided {@link WebClient.Builder}
     * @param baseUrl Base URL of the IMDb API (Default: https://api.imdbapi.dev)
     */
    public IMDbClient(WebClient.Builder builder,
                      @Value("${imdb.base-url:https://api.imdbapi.dev}") String baseUrl) {
        this.web = builder.baseUrl(baseUrl).build();
    }

    /**
     * Searches for movies and series on IMDb and returns the raw JSON response.
     *
     * Note: Blocks the calling thread until the response is received (simplified usage).
     *
     * @param query the search term
     * @return JSON response as a String
     */
    @Cacheable(CacheConfig.IMDB_SEARCH)
    public String searchMovieAndSeries(String query) {
        return web.get()
                .uri(u -> u.path("/search/titles")
                        .queryParam("query", query)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}