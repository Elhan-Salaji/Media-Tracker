package app.mediatracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Configuration for HTTP clients.
 *
 * Purpose: Exposes a preconfigured {@link WebClient.Builder} so HTTP clients (e.g., API clients)
 * can share consistent defaults.
 */
@Configuration
public class WebClientConfig {
    /**
     * Shared WebClient builder.
     *
     * Note: Sets a descriptive User-Agent for external APIs.
     * Compression stays off. With the default connector Reactor Netty sends "Accept-Encoding: gzip",
     * and Jikan answers every Accept-Encoding header with 504 (#81). All search sources therefore
     * receive uncompressed responses.
     * Additional defaults (timeouts, logging, proxy) can be configured here centrally.
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().compress(false)))
                .defaultHeader(HttpHeaders.USER_AGENT, "MediaTracker/1.0 (+localhost)");
    }
}
