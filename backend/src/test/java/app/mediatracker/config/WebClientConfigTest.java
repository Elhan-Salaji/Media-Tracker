package app.mediatracker.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for WebClientConfig.
 * An empty HTTP_USER_AGENT in backend/.env resolves to an empty string instead of the default, and the
 * search APIs would then receive a request without a usable User-Agent.
 */
class WebClientConfigTest {

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void webClientBuilder_rejectsBlankUserAgent(String userAgent) {
        // Arrange
        WebClientConfig config = new WebClientConfig();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> config.webClientBuilder(userAgent));
    }
}
