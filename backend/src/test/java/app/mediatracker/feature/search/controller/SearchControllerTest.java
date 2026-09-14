package app.mediatracker.feature.search.controller;

import app.mediatracker.config.TestSecurityConfig;
import app.mediatracker.feature.search.core.dto.SearchResult;
import app.mediatracker.feature.search.core.service.SearchService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for SearchController.
 * This class tests the mapping of search requests to the SearchService and
 * verifies the JSON structure of the returned results.
 */
@WebMvcTest(SearchController.class)
@ContextConfiguration(classes = {SearchController.class, TestSecurityConfig.class})
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @Test
    void testSearchReturnsResults() throws Exception {
        // Arrange: Setup a mock search result with metadata
        SearchResult result = new SearchResult();
        result.setId("1");
        result.setType("anime");
        result.setTitle("Naruto");
        result.setImageUrl("https://api.jikan.moe/v4/anime/1/image.jpg");
        result.setSourceUrl("https://api.jikan.moe/v4/anime/1");
        result.setMeta(Map.of("episodes", 220));

        Mockito.when(searchService.search(anyString(), anySet(), anyInt()))
                .thenReturn(List.of(result));

        // Act & Assert: Verify that parameters are passed and JSON path mapping is correct
        mockMvc.perform(get("/api/search")
                        .param("q", "Naruto")
                        .param("types", "anime")
                        .param("limit", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Naruto"))
                .andExpect(jsonPath("$[0].type").value("anime"))
                .andExpect(jsonPath("$[0].meta.episodes").value(220));
    }

    @Test
    void testSearchEmptyResults() throws Exception {
        // Arrange: Mock an empty response from the service
        Mockito.when(searchService.search(anyString(), anySet(), anyInt()))
                .thenReturn(List.of());

        // Act & Assert: Verify that an empty JSON array is returned
        mockMvc.perform(get("/api/search")
                        .param("q", "Unknown")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void testSearchWithNoTypesParameter() throws Exception {
        // Arrange: Setup a generic search result
        SearchResult result = new SearchResult();
        result.setId("2");
        result.setType("movie");
        result.setTitle("Inception");

        Mockito.when(searchService.search(anyString(), anySet(), anyInt()))
                .thenReturn(List.of(result));

        // Act & Assert: Ensure the search works even if 'types' is missing
        mockMvc.perform(get("/api/search")
                        .param("q", "Inception")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Inception"))
                .andExpect(jsonPath("$[0].type").value("movie"));
    }

    @Test
    void testSearchWithEmptyTypes() throws Exception {
        // Arrange
        SearchResult result = new SearchResult();
        result.setId("3");
        result.setType("anime");
        result.setTitle("Bleach");

        Mockito.when(searchService.search(anyString(), anySet(), anyInt()))
                .thenReturn(List.of(result));

        // Act & Assert: Verify handling of empty string in the types parameter
        mockMvc.perform(get("/api/search")
                        .param("q", "Bleach")
                        .param("types", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Bleach"))
                .andExpect(jsonPath("$[0].type").value("anime"));
    }

    @Test
    void testSearchWithEmptyLimitFallsBackToDefault() throws Exception {
        // Arrange
        Mockito.when(searchService.search(anyString(), anySet(), anyInt()))
                .thenReturn(List.of());

        // Act & Assert: The request the main page sends on load, with an empty query and limit
        mockMvc.perform(get("/api/search")
                        .param("q", "")
                        .param("types", "anime")
                        .param("limit", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(searchService).search("", Set.of("anime"), 24);
    }
}