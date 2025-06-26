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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import me.schf.api.model.PostEntity;
import me.schf.api.service.PostEntityService;

@WebMvcTest(PostController.class)
class PostControllerTests {
	
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private PostEntityService postEntityService;
    
	private final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void test_getRecentPosts_limitTooLow_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/posts/recent")
                .param("limit", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("Limit must be between 1 and 100"));
    }

    @Test
    void test_getRecentPosts_limitTooHigh_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/posts/recent")
                .param("limit", "101"))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("Limit must be between 1 and 100"));
    }

	@Test
    void test_getRecentPosts_limitValid_shouldReturnPosts() throws Exception {
		PostEntity dummyPostEntity = dummyPostEntity();
        when(postEntityService.getRecentPosts(10))
            .thenReturn(List.of(dummyPostEntity()));

        mockMvc.perform(get("/posts/recent"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].markdownText").value(dummyPostEntity.getMarkdownText()))
        .andExpect(jsonPath("$[0].author").value(dummyPostEntity.getAuthor()));
    }
	
	@Test
    void test_createOrUpdatePost_blank_shouldReturnBadRequest() throws Exception {
	    mockMvc.perform(post("/posts")
	            .contentType(MediaType.APPLICATION_JSON)
	            .content(""))  // blank
	    .andExpect(status().isBadRequest());    }
	
	@Test
    void test_createOrUpdatePost_emptyJson_shouldReturnBadRequest() throws Exception {
		mockMvc.perform(post("/posts")
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dummyPost())))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.postHeadline.title").value("dummy title"))
            .andExpect(jsonPath("$.postHeadline.publicationDate").value("2025-06-25T22:39:22.849193-05:00"))
            .andExpect(jsonPath("$.postHeadline.blurb").value("dummy blurb."))
            .andExpect(jsonPath("$.author").value(dummyPost().author()));
    }

	
    @SuppressWarnings("unchecked")
	@Test
    void test_searchPosts_shouldReturnMatchingPosts() throws Exception {
        PostEntity probeEntity = dummyPostEntity();

        when(postEntityService.search(
                any(PostEntity.class),
                any(ZonedDateTime.class),
                any(ZonedDateTime.class),
                any(List.class)
        )).thenReturn(List.of(probeEntity));

        String jsonProbe = objectMapper.writeValueAsString(dummyPost());

        mockMvc.perform(post("/posts/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonProbe)
                .param("from", "2023-01-01T00:00:00Z")
                .param("to", "2023-12-31T23:59:59Z")
                .param("tags", "tag1,tag2"))
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

        mockMvc.perform(delete("/posts/123"))
            .andExpect(status().isNoContent());

        verify(postEntityService, times(1)).delete(any(PostEntity.class));
    }

	
    @Test
    void test_deletePost_invalidId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/posts/")) // empty id in path (won't match mapping) 
            .andExpect(status().isNotFound()); 

        mockMvc.perform(delete("/posts/ "))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("Id must not be blank"));

        mockMvc.perform(delete("/posts/null"))
            .andExpect(status().isNotFound());
    }

	
    @Test
    void test_deletePost_notFound_shouldReturnNotFound() throws Exception {
        when(postEntityService.findById("notfound")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/posts/notfound"))
            .andExpect(status().isNotFound());
    }

	
    @Test
    void test_getPostById_valid_shouldReturnPost() throws Exception {
        when(postEntityService.findById("123")).thenReturn(Optional.of(dummyPostEntity()));

        mockMvc.perform(get("/posts/123"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.postHeadline.title").value("dummy title"))
            .andExpect(jsonPath("$.postHeadline.publicationDate").value("2025-06-25T22:39:22.849193-05:00"))
            .andExpect(jsonPath("$.postHeadline.blurb").value("dummy blurb."));
    }

	
    @Test
    void test_getPostById_blankId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/posts/ "))
            .andExpect(status().isBadRequest())
            .andExpect(status().reason("Id must not be blank"));
    }
	
    @Test
    void test_getPostById_notFound_shouldReturnNotFound() throws Exception {
        when(postEntityService.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/posts/missing"))
            .andExpect(status().isNotFound());
    }

	
	
	
	

}
