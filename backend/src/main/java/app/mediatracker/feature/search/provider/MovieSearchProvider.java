package app.mediatracker.feature.search.provider;

import app.mediatracker.feature.search.client.movie_and_series.TmdbClient;
import app.mediatracker.feature.search.core.dto.SearchResult;
import app.mediatracker.feature.search.core.provider.SearchProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "search.movie", name = "enabled", havingValue = "true")
public class MovieSearchProvider implements SearchProvider {

    private final TmdbClient tmdb;
    private final ObjectMapper mapper;
    private final String imageBaseUrl;

    /**
     * Constructor with dependencies.
     *
     * @param tmdb         HTTP client for the TMDB API
     * @param mapper       Jackson Mapper for parsing JSON responses
     * @param imageBaseUrl prefix for the relative poster paths TMDB returns
     */
    public MovieSearchProvider(TmdbClient tmdb, ObjectMapper mapper, @Value("${tmdb.image-base-url}") String imageBaseUrl) {
        this.tmdb = tmdb;
        this.mapper = mapper;
        this.imageBaseUrl = imageBaseUrl;
    }

    /**
     * Returns the type name of this provider.
     *
     * @return "movie"
     */
    @Override
    public String getType() {
        return "movie";
    }

    /**
     * Searches for movies via the TMDB API.
     *
     * Behavior: Parses the response, extracts relevant fields, and returns
     * a normalized list. Errors are logged and result in an empty list.
     *
     * @param searchQuery the search term
     * @param limit       maximum number of results
     * @return List of {@link SearchResult}
     */
    @Override
    public List<SearchResult> search(String searchQuery, int limit) {
        try {
            String json = tmdb.searchMovie(searchQuery);
            JsonNode results = mapper.readTree(json).path("results");

            List<SearchResult> searchResults = new ArrayList<>();
            for (JsonNode movieNode : results) {
                String id     = movieNode.path("id").asText();
                String title  = movieNode.path("title").asText("");
                String poster = movieNode.path("poster_path").asText("");
                String img    = poster.isBlank() ? "" : imageBaseUrl + poster;
                String url    = "https://www.themoviedb.org/movie/" + id;

                // optional extras in meta
                Map<String, Object> meta = new HashMap<>();
                String released = movieNode.path("release_date").asText("");
                if (released.length() >= 4) meta.put("year", Integer.parseInt(released.substring(0, 4)));

                searchResults.add(SearchResult.builder()
                        .type("movie")
                        .id(id)
                        .title(title)
                        .imageUrl(img)
                        .sourceUrl(url)
                        .meta(meta.isEmpty() ? null : meta)
                        .build());

                if (searchResults.size() >= limit) break;
            }

            return searchResults;
        } catch (Exception e) {
            log.warn("Movie search failed: {}", e.getMessage());
            return List.of();
        }
    }
}