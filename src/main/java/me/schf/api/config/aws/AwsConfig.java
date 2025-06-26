package me.schf.api.config.aws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;

@Configuration
public class AwsConfig {
	
	@FunctionalInterface
	public interface SsmClientProvider {
	    SsmClient getClient();
	}

    @Bean
    SsmClientProvider ssmClientProvider() {
        return SsmClient::create;
    }
    
    @FunctionalInterface
    public interface ParameterRetriever {
    	public String getParameter(String parameterName);
    }
    
    @Bean("awsParameterRetriever")
    ParameterRetriever awsParameterRetriever(SsmClientProvider ssmClientProvider) {
        return parameterName -> {
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(parameterName)
                    .withDecryption(true)
                    .build();

            SsmClient ssmClient = ssmClientProvider.getClient();
            return ssmClient.getParameter(request).parameter().value();
        };
    }

}
