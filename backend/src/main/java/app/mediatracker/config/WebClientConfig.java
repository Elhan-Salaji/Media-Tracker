package app.mediatracker.config;

import org.springframework.beans.factory.annotation.Value;
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
     * Note: Every request carries the User-Agent from "web-client.user-agent", so the search APIs can
     * tell which application calls them and whom to contact (#12). Open Library asks for that.
     * Compression stays off. With the default connector Reactor Netty sends "Accept-Encoding: gzip",
     * and Jikan answers every Accept-Encoding header with 504 (#81). All search sources therefore
     * receive uncompressed responses.
     * Additional defaults (timeouts, logging, proxy) can be configured here centrally.
     *
     * @param userAgent value of the User-Agent header; must not be blank
     */
    @Bean
    public WebClient.Builder webClientBuilder(@Value("${web-client.user-agent}") String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            throw new IllegalStateException(
                    "web-client.user-agent is empty. Remove HTTP_USER_AGENT or give it a value, see backend/.env.example.");
        }
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().compress(false)))
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent);
    }
}
