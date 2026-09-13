package app.mediatracker.config;

import app.mediatracker.feature.search.client.movie_and_series.IMDbClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that the search clients answer a repeated query from the cache.
 *
 * The test builds a small Spring context with {@link CacheConfig} and a real {@link IMDbClient},
 * whose WebClient talks to a mocked {@link ExchangeFunction}. Counting the exchanges shows how
 * often the client went out to the external API.
 */
@SpringJUnitConfig(SearchClientCachingTest.TestConfig.class)
class SearchClientCachingTest {

    private static final String JSON_RESPONSE = """
            { "titles": [ { "id": "tt1190634", "primaryTitle": "The Boys" } ] }
            """;

    @Configuration
    @Import(CacheConfig.class)
    static class TestConfig {

        @Bean
        ExchangeFunction exchangeFunction() {
            return mock(ExchangeFunction.class);
        }

        @Bean
        IMDbClient imdbClient(ExchangeFunction exchangeFunction) {
            return new IMDbClient(WebClient.builder().exchangeFunction(exchangeFunction), "https://api.imdbapi.dev");
        }
    }

    @Autowired
    private IMDbClient imdbClient;

    @Autowired
    private ExchangeFunction exchangeFunction;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        clearInvocations(exchangeFunction);
        // A response body can be read once, so every exchange gets a fresh response.
        when(exchangeFunction.exchange(any())).thenAnswer(invocation -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(JSON_RESPONSE)
                        .build()));
    }

    @Test
    void repeatedSearch_isServedFromCache() {
        String first = imdbClient.searchMovieAndSeries("the boys");
        String second = imdbClient.searchMovieAndSeries("the boys");

        assertEquals(JSON_RESPONSE, first);
        assertEquals(first, second);
        verify(exchangeFunction, times(1)).exchange(any());
    }

    @Test
    void differentQuery_callsTheApiAgain() {
        imdbClient.searchMovieAndSeries("the boys");
        imdbClient.searchMovieAndSeries("the office");

        verify(exchangeFunction, times(2)).exchange(any());
    }

    @Test
    void everyCacheUsedByASearchClient_isRegistered() {
        List<String> cacheNames = Arrays.stream(new Class<?>[]{
                        app.mediatracker.feature.search.client.anime.JikanAnimeClient.class,
                        app.mediatracker.feature.search.client.manga.JikanMangaClient.class,
                        app.mediatracker.feature.search.client.movie_and_series.IMDbClient.class,
                        app.mediatracker.feature.search.client.game.RawgClient.class,
                        app.mediatracker.feature.search.client.book.OpenLibraryClient.class,
                        app.mediatracker.feature.search.client.music.ItunesClient.class})
                .flatMap(client -> Arrays.stream(client.getDeclaredMethods()))
                .map(method -> method.getAnnotation(Cacheable.class))
                .filter(annotation -> annotation != null)
                .flatMap(annotation -> Arrays.stream(annotation.value()))
                .toList();

        assertEquals(6, cacheNames.size(), "each of the six search clients caches its search");
        cacheNames.forEach(name -> assertNotNull(cacheManager.getCache(name), "missing cache " + name));
    }
}
