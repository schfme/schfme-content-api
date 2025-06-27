package me.schf.api.config.app;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.parameter-names")
public class ParameterNamesConfig {

	private String apiKeysPath;
	private String clusterName;
	private String databaseName;
	private String host;
	private String serviceName;
	private String servicePassword;

	public String getApiKeysPath() {
		return apiKeysPath;
	}

	public String getClusterName() {
		return clusterName;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public String getHost() {
		return host;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getServicePassword() {
		return servicePassword;
	}

	public void setApiKeysPath(String apiKeysPath) {
		this.apiKeysPath = apiKeysPath;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setServicePassword(String servicePassword) {
		this.servicePassword = servicePassword;
	}
}