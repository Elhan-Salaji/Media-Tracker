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
@ConditionalOnProperty(prefix = "search.series", name = "enabled", havingValue = "true")
public class SeriesSearchProvider implements SearchProvider {
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
    public SeriesSearchProvider(TmdbClient tmdb, ObjectMapper mapper, @Value("${tmdb.image-base-url}") String imageBaseUrl) {
        this.tmdb = tmdb;
        this.mapper = mapper;
        this.imageBaseUrl = imageBaseUrl;
    }

    /**
     * Returns the type name of this provider.
     *
     * @return "series"
     */
    @Override
    public String getType() {
        return "series";
    }

    /**
     * Searches for series via the TMDB API.
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
            String json = tmdb.searchSeries(searchQuery);
            JsonNode results = mapper.readTree(json).path("results");

            List<SearchResult> searchResults = new ArrayList<>();
            for (JsonNode seriesNode : results) {
                String id     = seriesNode.path("id").asText();
                String title  = seriesNode.path("name").asText("");
                String poster = seriesNode.path("poster_path").asText("");
                String img    = poster.isBlank() ? "" : imageBaseUrl + poster;
                String url    = "https://www.themoviedb.org/tv/" + id;

                // optional extras in meta
                Map<String, Object> meta = new HashMap<>();
                String firstAired = seriesNode.path("first_air_date").asText("");
                if (firstAired.length() >= 4) meta.put("year", Integer.parseInt(firstAired.substring(0, 4)));

                searchResults.add(SearchResult.builder()
                        .type("series")
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
            log.warn("Series search failed: {}", e.getMessage());
            return List.of();
        }
    }
}