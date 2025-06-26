package me.schf.api;

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
		return input -> "minecraft";
	}
	
    @Primary
    @Bean
    MongoClient mongoClient() {
        return Mockito.mock(MongoClient.class);
    }

}
