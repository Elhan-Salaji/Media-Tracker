package app.mediatracker.feature.search.client.movie_and_series;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cache.annotation.Cacheable;
import app.mediatracker.config.CacheConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@ConditionalOnExpression("${search.movie.enabled:false} or ${search.series.enabled:false}")
public class TmdbClient {

    private final WebClient web;
    private final String apiKey;

    public TmdbClient(WebClient.Builder builder,
                      @Value("${tmdb.base-url:https://api.themoviedb.org/3}") String baseUrl,
                      @Value("${tmdb.api-key}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("tmdb.api-key is empty. Set TMDB_API_KEY, see backend/.env.example.");
        }
        this.web = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    @Cacheable(CacheConfig.TMDB_MOVIE_SEARCH)
    public String searchMovie(String query) {
        return search("/search/movie", query);
    }

    @Cacheable(CacheConfig.TMDB_SERIES_SEARCH)
    public String searchSeries(String query) {
        return search("/search/tv", query);
    }

    private String search(String path, String query) {
        return web.get()
                .uri(u -> u.path(path)
                        .queryParam("query", query)
                        .queryParam("include_adult", "false")
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
