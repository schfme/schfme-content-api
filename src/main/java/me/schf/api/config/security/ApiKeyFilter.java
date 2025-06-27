package me.schf.api.config.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.schf.api.config.app.ParameterNamesConfig;
import me.schf.api.config.aws.AwsConfig.ParameterRetriever;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

	private final Set<String> validApiKeys;

	public ApiKeyFilter(ParameterRetriever awsParameterRetriever, ParameterNamesConfig parameterNamesConfig) {
		validApiKeys = new HashSet<>(
				awsParameterRetriever.getParametersByPath(parameterNamesConfig.getApiKeysPath(), true).values());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String requestApiKey = request.getHeader("X-API-KEY");

		if (validApiKeys.contains(requestApiKey)) {
			filterChain.doFilter(request, response);
		} else {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Unauthorized - Invalid API Key");
		}
	}

}
