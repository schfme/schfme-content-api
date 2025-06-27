package me.schf.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import me.schf.api.TestConfig;
import me.schf.api.service.PostEntityService;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestConfig.class})
class SwaggerAccessTests {
	
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
    void test_swaggerUi_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html").with(apiKeyHeader()))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Swagger UI")));
    }

    @Test
    void test_openApiDocs_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/v3/api-docs").with(apiKeyHeader()))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"));
    }
    
	@Test
	void test_getAnyDocs_noApiKey_shouldBeAccessedDenied() throws Exception {
		// no api key passed
		mockMvc.perform(get("/v3/api-docs")).andExpect(status().isUnauthorized());
	}

}
