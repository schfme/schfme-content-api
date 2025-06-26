package me.schf.api.config.app;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

	public enum AppEnvironment {
		TEST("test"), PROD("prod");

		private String name;

		private AppEnvironment(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private AppEnvironment environment;

	public AppEnvironment getEnvironment() {
		return environment;
	}

	public void setEnvironment(AppEnvironment environment) {
		this.environment = environment;
	}

}
