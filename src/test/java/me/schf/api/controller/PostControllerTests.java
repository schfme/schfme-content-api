package me.schf.api.controller;

import static me.schf.api.controller.TestPostFactory.dummyPost;
import static me.schf.api.controller.TestPostFactory.dummyPostEntity;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import me.schf.api.TestConfig;
import me.schf.api.config.app.ParameterNamesConfig;
import me.schf.api.model.PostEntity;
import me.schf.api.service.PostEntityService;

@Import({TestConfig.class, ParameterNamesConfig.class})
@WebMvcTest(PostController.class)
class PostControllerTests {
	
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
    
	private final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void test_getRecentPosts_limitTooLow_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/posts/recent")
                .with(apiKeyHeader())
                .param("limit", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("Limit must be between 1 and 100"));
    }

    @Test
    void test_getRecentPosts_limitTooHigh_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/posts/recent")
                .with(apiKeyHeader())
                .param("limit", "101"))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("Limit must be between 1 and 100"));
    }

	@Test
    void test_getRecentPosts_limitValid_shouldReturnPosts() throws Exception {
		PostEntity dummyPostEntity = dummyPostEntity();
        when(postEntityService.getRecentPosts(10))
            .thenReturn(List.of(dummyPostEntity()));

		mockMvc.perform(get("/posts/recent")
				.with(apiKeyHeader()))
		        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].markdownText").value(dummyPostEntity.getMarkdownText()))
        .andExpect(jsonPath("$[0].author").value(dummyPostEntity.getAuthor()));
    }
	
	@Test
    void test_createOrUpdatePost_blank_shouldReturnBadRequest() throws Exception {
	    mockMvc.perform(post("/posts")
	    		.with(apiKeyHeader())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(""))  // blank
	    .andExpect(status().isBadRequest());    }
	
	@Test
    void test_createOrUpdatePost_emptyJson_shouldReturnBadRequest() throws Exception {
		mockMvc.perform(post("/posts")
				.with(apiKeyHeader())
		        .contentType(MediaType.APPLICATION_JSON)
		        .content("{}"))  // empty json
		    .andExpect(status().isBadRequest())
		    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
		    .andExpect(jsonPath("$.postHeadline").value("Post headline must not be null"))
		    .andExpect(jsonPath("$.markdownText").value("Markdown text must not be blank"))
		    .andExpect(jsonPath("$.author").value("Author must not be blank"))
		    .andExpect(jsonPath("$.tags").value("Tags must not be null"));
    }
	
    @Test
    void test_createOrUpdatePost_valid_shouldReturnCreatedPost() throws Exception {
        when(postEntityService.add(any(PostEntity.class))).thenReturn(dummyPostEntity());

        mockMvc.perform(post("/posts")
        		.with(apiKeyHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dummyPost())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.postHeadline.title").value("dummy title"))
            .andExpect(jsonPath("$.postHeadline.publicationDate").value("2025-06-25T22:39:22.849193-05:00"))
            .andExpect(jsonPath("$.postHeadline.blurb").value("dummy blurb."))
            .andExpect(jsonPath("$.author").value(dummyPost().author()));
    }

	
    @Test
    void test_getPosts_shouldReturnMatchingPosts() throws Exception {
        PostEntity probeEntity = dummyPostEntity();

        when(postEntityService.search(
                any(PostEntity.class),
                any(ZonedDateTime.class),
                any(ZonedDateTime.class)
        )).thenReturn(List.of(probeEntity));

        mockMvc.perform(get("/posts")
                .with(apiKeyHeader())
                .param("title", "dummy title")
                .param("author", "dummy author")
                .param("sharePost", "true")
                .param("from", "2023-01-01T00:00:00Z")
                .param("to", "2023-12-31T23:59:59Z")
                .param("tags", "ART,ARTWARE"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].postHeadline.title").value("dummy title"))
            .andExpect(jsonPath("$[0].postHeadline.publicationDate").value("2025-06-25T22:39:22.849193-05:00"))
            .andExpect(jsonPath("$[0].postHeadline.blurb").value("dummy blurb."));
    }
	
    @Test
    void test_deletePost_validId_shouldReturnNoContent() throws Exception {
        when(postEntityService.findById("123")).thenReturn(Optional.of(dummyPostEntity()));
        doNothing().when(postEntityService).delete(any(PostEntity.class));

        mockMvc.perform(delete("/posts/123")
        		.with(apiKeyHeader()))
            .andExpect(status().isNoContent());

        verify(postEntityService, times(1)).delete(any(PostEntity.class));
    }

	
	@Test
	void test_deletePost_invalidId_shouldReturnBadRequest() throws Exception {
		mockMvc.perform(delete("/posts/")
				.with(apiKeyHeader())) // empty id in path (won't match mapping)
				.andExpect(status().isNotFound());

		mockMvc.perform(delete("/posts/ ")
				.with(apiKeyHeader())).andExpect(status().isBadRequest())
				.andExpect(status().reason("Id must not be blank"));

		mockMvc.perform(delete("/posts/null")
				.with(apiKeyHeader())).andExpect(status().isNotFound());
	}

	
	@Test
    void test_deletePost_notFound_shouldReturnNotFound() throws Exception {
        when(postEntityService.findById("notfound")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/posts/notfound").with(apiKeyHeader()))
            .andExpect(status().isNotFound());
    }
	
    @Test
    void test_getPostById_valid_shouldReturnPost() throws Exception {
        when(postEntityService.findById("123")).thenReturn(Optional.of(dummyPostEntity()));

        mockMvc.perform(get("/posts/123").with(apiKeyHeader()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.postHeadline.title").value("dummy title"))
            .andExpect(jsonPath("$.postHeadline.publicationDate").value("2025-06-25T22:39:22.849193-05:00"))
            .andExpect(jsonPath("$.postHeadline.blurb").value("dummy blurb."));
    }
	
    @Test
    void test_getPostById_blankId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/posts/ ").with(apiKeyHeader()))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("Id must not be blank"));
    }
	
	@Test
    void test_getPostById_notFound_shouldReturnNotFound() throws Exception {
        when(postEntityService.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/posts/missing").with(apiKeyHeader()))
            .andExpect(status().isNotFound());
    }
	
    @Test
    void test_getPostByTitle_valid_shouldReturnPost() throws Exception {
        when(postEntityService.findById("123")).thenReturn(Optional.of(dummyPostEntity()));

        mockMvc.perform(get("/posts/123").with(apiKeyHeader()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.postHeadline.title").value("dummy title"))
            .andExpect(jsonPath("$.postHeadline.publicationDate").value("2025-06-25T22:39:22.849193-05:00"))
            .andExpect(jsonPath("$.postHeadline.blurb").value("dummy blurb."));
    }
	
    @Test
    void test_getPostByTitle_blankId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/posts/ ").with(apiKeyHeader()))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("Id must not be blank"));
    }
	
	@Test
    void test_getPostByTitle_notFound_shouldReturnNotFound() throws Exception {
        when(postEntityService.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/posts/missing").with(apiKeyHeader()))
            .andExpect(status().isNotFound());
    }
	
	@Test
	void test_anyCall_noApiKey_shouldReturnNotAuthorized() throws Exception {
	    mockMvc.perform(get("/posts/123"))  // no api key header here
	        .andExpect(status().isUnauthorized());
	}

}
