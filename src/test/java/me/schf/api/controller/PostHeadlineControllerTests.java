package me.schf.api.controller;

import static me.schf.api.controller.TestPostFactory.dummyPostEntity;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import me.schf.api.TestConfig;
import me.schf.api.config.app.ParameterNamesConfig;
import me.schf.api.model.PostEntity;
import me.schf.api.service.PostEntityService;

@Import({TestConfig.class, ParameterNamesConfig.class})
@WebMvcTest(PostHeadlineController.class)
class PostHeadlineControllerTests {
	
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostEntityService postEntityService;
    
    private RequestPostProcessor apiKeyHeader() {
        return request -> {
            request.addHeader("X-API-Key", "dummy-api-key");
            return request;
        };
    }

    @Test
    void test_getRecentPostHeadlines_limitTooLow_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/posts/headlines/recent")
        		.with(apiKeyHeader())
                .param("limit", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("Limit must be between 1 and 100"));
    }

    @Test
    void test_getRecentPostHeadlines_limitTooHigh_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/posts/headlines/recent")
        		.with(apiKeyHeader())
                .param("limit", "101"))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("Limit must be between 1 and 100"));
    }

    @Test
    void test_getRecentPostHeadlines_limitValid_shouldReturnPostHeadlines() throws Exception {
        PostEntity dummy = dummyPostEntity();
        when(postEntityService.getRecentPosts(anyInt())).thenReturn(List.of(dummy));

		mockMvc.perform(get("/posts/headlines/recent").with(apiKeyHeader()))
	            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].title").value("dummy title"))
            .andExpect(jsonPath("$[0].publicationDate").value("2025-06-25T22:39:22.849193-05:00"))
            .andExpect(jsonPath("$[0].blurb").value("dummy blurb."));
    }

    @Test
    void test_getPostHeadlineById_blankId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/posts/headlines/ ").with(apiKeyHeader()))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("Id must not be null or blank"));
    }

    @Test
    void test_getPostHeadlineById_validId_shouldReturnPostHeadline() throws Exception {
        PostEntity dummy = dummyPostEntity();
        when(postEntityService.findById(anyString())).thenReturn(Optional.of(dummy));

        mockMvc.perform(get("/posts/headlines/123").with(apiKeyHeader()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value("dummy title"))
            .andExpect(jsonPath("$.publicationDate").value("2025-06-25T22:39:22.849193-05:00"))
            .andExpect(jsonPath("$.blurb").value("dummy blurb."));
    }

    @Test
    void test_getPostHeadlineById_notFound_shouldReturnNotFound() throws Exception {
        when(postEntityService.findById(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/posts/headlines/missing").with(apiKeyHeader()))
            .andExpect(status().isNotFound());
    }
    
	@Test
	void test_anyCall_noApiKey_shouldReturnNotAuthorized() throws Exception {
	    mockMvc.perform(get("/posts/headlines/123"))  // no api key header here
	        .andExpect(status().isUnauthorized());
	}

}
