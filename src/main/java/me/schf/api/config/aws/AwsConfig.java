package me.schf.api.config.aws;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;

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
    
    public interface ParameterRetriever {
        String getParameter(String parameterName);
        Map<String, String> getParametersByPath(String path, boolean recursive);
    }

    @Bean("awsParameterRetriever")
    ParameterRetriever awsParameterRetriever(SsmClientProvider ssmClientProvider) {
        return new ParameterRetriever() {
            @Override
            public String getParameter(String parameterName) {
                SsmClient ssmClient = ssmClientProvider.getClient();
                GetParameterRequest request = GetParameterRequest.builder()
                        .name(parameterName)
                        .withDecryption(true)
                        .build();

                return ssmClient.getParameter(request).parameter().value();
            }

            @Override
            public Map<String, String> getParametersByPath(String path, boolean recursive) {
                SsmClient ssmClient = ssmClientProvider.getClient();
                Map<String, String> parameters = new HashMap<>();
                String nextToken = null;

                do {
                    GetParametersByPathRequest request = GetParametersByPathRequest.builder()
                            .path(path)
                            .withDecryption(true)
                            .recursive(recursive)
                            .nextToken(nextToken)
                            .build();

                    GetParametersByPathResponse response = ssmClient.getParametersByPath(request);

                    response.parameters().forEach(param -> parameters.put(param.name(), param.value()));
                    nextToken = response.nextToken();

                } while (nextToken != null);

                return parameters;
            }
        };
    }

}
