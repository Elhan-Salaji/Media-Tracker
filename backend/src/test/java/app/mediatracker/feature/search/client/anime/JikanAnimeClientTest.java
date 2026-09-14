package app.mediatracker.feature.search.client.anime;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for JikanAnimeClient.
 * This test demonstrates how to mock the reactive WebClient to verify
 * outgoing HTTP requests and their responses without an actual network connection.
 */
class JikanAnimeClientTest {

    @Test
    void searchAnime_returnsJsonResponse() {
        // Arrange: Prepare the expected JSON response from the Jikan API
        String jsonResponse = """
            {
              "data": [
                { "title": "Naruto" }
              ]
            }
            """;

        // We mock the ExchangeFunction, which is the internal component WebClient uses to execute requests
        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);

        // Define the behavior: Return a 200 OK response containing our JSON string
        when(exchangeFunction.exchange(any()))
                .thenReturn(Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(jsonResponse)
                        .build()));

        // Inject the mocked function into a WebClient builder
        WebClient.Builder builder = WebClient.builder().exchangeFunction(exchangeFunction);

        // Instantiate the client with the mocked builder and a dummy base URL
        JikanAnimeClient client = new JikanAnimeClient(builder, "https://api.jikan.moe/v4");

        // Act: Execute the search logic
        String result = client.searchAnime("naruto");

        // Assert: Verify the result matches the mock and the request was actually triggered
        assertEquals(jsonResponse, result);
        verify(exchangeFunction, times(1)).exchange(any());
    }

    @Test
    void searchAnime_sendsQueryAsParameter() {
        // Arrange
        ExchangeFunction exchangeFunction = exchangeReturningEmptyData();
        JikanAnimeClient client = new JikanAnimeClient(
                WebClient.builder().exchangeFunction(exchangeFunction), "https://api.jikan.moe/v4");

        // Act
        client.searchAnime("naruto");

        // Assert: The search term arrives as the q parameter
        assertEquals("https://api.jikan.moe/v4/anime?q=naruto", requestedUrl(exchangeFunction));
    }

    @Test
    void searchAnime_leavesEmptyQueryOut() {
        // Arrange: Jikan answers "q=" with 504, the main page sends an empty query on load
        ExchangeFunction exchangeFunction = exchangeReturningEmptyData();
        JikanAnimeClient client = new JikanAnimeClient(
                WebClient.builder().exchangeFunction(exchangeFunction), "https://api.jikan.moe/v4");

        // Act
        client.searchAnime("");

        // Assert: The request goes to /anime without a q parameter
        assertEquals("https://api.jikan.moe/v4/anime", requestedUrl(exchangeFunction));
    }

    private static ExchangeFunction exchangeReturningEmptyData() {
        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
        when(exchangeFunction.exchange(any()))
                .thenReturn(Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body("{ \"data\": [] }")
                        .build()));
        return exchangeFunction;
    }

    private static String requestedUrl(ExchangeFunction exchangeFunction) {
        ArgumentCaptor<ClientRequest> request = ArgumentCaptor.forClass(ClientRequest.class);
        verify(exchangeFunction).exchange(request.capture());
        return request.getValue().url().toString();
    }
}
