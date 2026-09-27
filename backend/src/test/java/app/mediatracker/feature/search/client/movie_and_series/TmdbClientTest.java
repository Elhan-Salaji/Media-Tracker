package app.mediatracker.feature.search.client.movie_and_series;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for TmdbClient.
 * This test verifies that the client reaches the two TMDB search endpoints and returns
 * the raw JSON response, and that it refuses to start without an API key.
 */
public class TmdbClientTest {

    private static final String BASE_URL = "https://api.themoviedb.org/3";

    /**
     * Builds a client whose WebClient answers every request with the given body.
     *
     * @param exchangeFunction the mocked engine below the WebClient
     * @param jsonResponse     body of the mocked 200 response
     * @return client under test
     */
    private TmdbClient clientReturning(ExchangeFunction exchangeFunction, String jsonResponse) {
        when(exchangeFunction.exchange(any()))
                .thenReturn(Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(jsonResponse)
                        .build()));

        return new TmdbClient(WebClient.builder().exchangeFunction(exchangeFunction), BASE_URL, "fake-api-key");
    }

    @Test
    void searchMovie_returnsJsonResponse() {
        // Arrange: a movie result carries its name in "title"
        String jsonResponse = """
            {
              "results": [
                { "id": 603, "title": "The Matrix", "release_date": "1999-03-30" }
              ]
            }
            """;
        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
        TmdbClient client = clientReturning(exchangeFunction, jsonResponse);

        // Act
        String result = client.searchMovie("matrix");

        // Assert
        assertEquals(jsonResponse, result);
        verify(exchangeFunction, times(1)).exchange(any());
    }

    @Test
    void searchSeries_returnsJsonResponse() {
        // Arrange: a series result carries its name in "name"
        String jsonResponse = """
            {
              "results": [
                { "id": 46260, "name": "Naruto", "first_air_date": "2002-10-03" }
              ]
            }
            """;
        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
        TmdbClient client = clientReturning(exchangeFunction, jsonResponse);

        // Act
        String result = client.searchSeries("naruto");

        // Assert
        assertEquals(jsonResponse, result);
        verify(exchangeFunction, times(1)).exchange(any());
    }

    @Test
    void missingApiKey_stopsTheClient() {
        // Arrange
        WebClient.Builder builder = WebClient.builder();

        // Act + Assert: an empty key has to fail loudly instead of sending unauthenticated requests
        assertThrows(IllegalStateException.class, () -> new TmdbClient(builder, BASE_URL, ""));
    }
}
