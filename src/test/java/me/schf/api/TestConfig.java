package me.schf.api;

import java.util.Map;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.mongodb.client.MongoClient;

import me.schf.api.config.aws.AwsConfig.ParameterRetriever;

@TestConfiguration
public class TestConfig {

	@Primary
	@Bean("testAwsParameterRetriever")  
	ParameterRetriever awsParameterRetriever() {
		return new ParameterRetriever() {
			
			@Override
			public Map<String, String> getParametersByPath(String path, boolean recursive) {
				return Map.of("map-key", "dummy-api-key");
			}
			
			@Override
			public String getParameter(String parameterName) {
				return "dummy-parameter";
			}
		};
	}
	
    @Primary
    @Bean
    MongoClient mongoClient() {
        return Mockito.mock(MongoClient.class);
    }

}
