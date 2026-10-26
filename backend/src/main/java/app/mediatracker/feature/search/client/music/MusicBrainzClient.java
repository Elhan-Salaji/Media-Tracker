package app.mediatracker.feature.search.client.music;

import app.mediatracker.config.CacheConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Minimal HTTP client for the MusicBrainz web service.
 *
 * Purpose: Searches release groups, the album-level works of MusicBrainz, and returns the raw
 * JSON response. A release group bundles every edition of an album, so one album appears once
 * instead of once per pressing or remaster.
 *
 * Configuration: Base URL can be overridden via "musicbrainz.base-url". MusicBrainz needs no
 * API key, but it expects a User-Agent with application name, version and contact, which the
 * shared {@link WebClient.Builder} sets on every request.
 *
 * Activation: Only loaded if "search.music.enabled=true" is set, like {@code MusicSearchProvider}.
 */
@Component
@ConditionalOnProperty(prefix = "search.music", name = "enabled", havingValue = "true")
public class MusicBrainzClient {

    private final WebClient web;

    /**
     * Creates the client with a predefined base URL.
     *
     * @param builder Spring-provided {@link WebClient.Builder}
     * @param baseUrl Base URL of the MusicBrainz web service (default: https://musicbrainz.org/ws/2)
     */
    public MusicBrainzClient(WebClient.Builder builder,
                             @Value("${musicbrainz.base-url:https://musicbrainz.org/ws/2}") String baseUrl) {
        this.web = builder.baseUrl(baseUrl).build();
    }

    /**
     * Searches release groups on MusicBrainz and returns the raw JSON response.
     *
     * Note: Blocks the calling thread until the response is received. The query is passed as a
     * URI variable, so characters such as "{" or "&" in the search term are encoded instead of
     * being read as part of the URI.
     *
     * @param query the search term
     * @param limit maximum number of results (MusicBrainz accepts 1 to 100)
     * @return JSON response as a String
     */
    @Cacheable(CacheConfig.MUSICBRAINZ_SEARCH)
    public String searchReleaseGroups(String query, int limit) {
        return web.get()
                .uri(u -> u.path("/release-group")
                        .queryParam("query", "{query}")
                        .queryParam("limit", limit)
                        .queryParam("fmt", "json")
                        .build(query))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
