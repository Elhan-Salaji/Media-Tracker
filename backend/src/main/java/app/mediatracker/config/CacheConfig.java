package app.mediatracker.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Caches the raw responses of the external search APIs.
 *
 * Purpose: A search without a type filter calls every enabled provider, and each provider
 * calls its external API with a blocking request. The caches let a repeated search with the
 * same query come back from memory instead, which keeps the search fast and the external
 * rate limits out of reach.
 *
 * Every cache is registered by name with its own expiry. The manager creates no caches on
 * the fly, so a {@code @Cacheable} name that is missing here fails on the first call instead
 * of running without an expiry.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String JIKAN_ANIME_SEARCH = "jikanAnimeSearch";
    public static final String JIKAN_MANGA_SEARCH = "jikanMangaSearch";
    public static final String IMDB_SEARCH = "imdbSearch";
    public static final String RAWG_SEARCH = "rawgSearch";
    public static final String OPEN_LIBRARY_SEARCH = "openLibrarySearch";
    public static final String ITUNES_SEARCH = "itunesSearch";

    /**
     * One hour keeps a title released today findable on the same day.
     */
    static final Duration DEFAULT_TTL = Duration.ofHours(1);

    /**
     * Jikan allows three requests per second and 60 per minute, the tightest limit of all
     * sources, and anime and manga catalogues change slowly. Its caches keep entries longer.
     */
    static final Duration JIKAN_TTL = Duration.ofHours(6);

    /**
     * Upper bound per cache, so a burst of distinct queries cannot grow the heap without limit.
     */
    static final long MAX_ENTRIES_PER_CACHE = 500;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // An empty static name list switches off the on-the-fly creation of unknown caches.
        cacheManager.setCacheNames(List.of());
        register(cacheManager, JIKAN_ANIME_SEARCH, JIKAN_TTL);
        register(cacheManager, JIKAN_MANGA_SEARCH, JIKAN_TTL);
        register(cacheManager, IMDB_SEARCH, DEFAULT_TTL);
        register(cacheManager, RAWG_SEARCH, DEFAULT_TTL);
        register(cacheManager, OPEN_LIBRARY_SEARCH, DEFAULT_TTL);
        register(cacheManager, ITUNES_SEARCH, DEFAULT_TTL);
        return cacheManager;
    }

    private static void register(CaffeineCacheManager cacheManager, String name, Duration ttl) {
        cacheManager.registerCustomCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(MAX_ENTRIES_PER_CACHE)
                .build());
    }
}
