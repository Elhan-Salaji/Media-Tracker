package app.mediatracker.feature.search.client.music;

import app.mediatracker.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Minimal HTTP client for the iTunes Search API.
 *
 * Purpose: Provides a method to retrieve music tracks based on a search term
 * as a raw JSON response.
 *
 * Configuration: Base URL can be overridden via "itunes.base-url".
 */
@Component
public class ItunesClient {

    private final WebClient web;

    /**
     * Creates the client with a predefined base URL.
     *
     * @param builder Spring-provided {@link WebClient.Builder}
     * @param baseUrl Base URL of the iTunes API (default: https://itunes.apple.com)
     */
    public ItunesClient(WebClient.Builder builder,
                        @Value("${itunes.base-url:https://itunes.apple.com}") String baseUrl) {
        this.web = builder.baseUrl(baseUrl).build();
    }

    /**
     * Searches for music tracks via the iTunes Search API and returns the raw JSON response.
     *
     * @param term  the search term
     * @param limit maximum number of results
     * @return JSON response as a String
     */
    @Cacheable(CacheConfig.ITUNES_SEARCH)
    public String searchTracks(String term, int limit) {
        return web.get()
                .uri(u -> u.path("/search")
                        .queryParam("term", term)
                        .queryParam("media", "music")
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}