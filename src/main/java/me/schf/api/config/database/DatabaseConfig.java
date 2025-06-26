package me.schf.api.config.database;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import me.schf.api.config.app.ParameterNamesConfig;
import me.schf.api.config.aws.AwsConfig.ParameterRetriever;

@Configuration
public class DatabaseConfig {
	
	@FunctionalInterface
	public interface DatabaseConnection {
		public String getConnectionUri();
	}
	
	public record MongoConnection(	    
			String cluster,
		    String clusterHost,
		    String dbName,
		    String dbPassword,
		    String dbUser
		   ) implements DatabaseConnection {

		@Override
		public String getConnectionUri() {
	        return "mongodb+srv://%s:%s@%s/?retryWrites=true&w=majority&appName=%s"
	                .formatted(dbUser, dbPassword, clusterHost, cluster);
		}

	}

	@Bean
	DatabaseConnection mongoConnection(ParameterRetriever awsParameterRetriever, ParameterNamesConfig parameterNamesConfig) {
	    String clusterNameParam = parameterNamesConfig.getClusterName();
	    String hostParam = parameterNamesConfig.getHost();
	    String dbNameParam = parameterNamesConfig.getDatabaseName();
	    String serviceNameParam = parameterNamesConfig.getServiceName();
	    String servicePasswordParam = parameterNamesConfig.getServicePassword();

	    String clusterName = awsParameterRetriever.getParameter(clusterNameParam);
	    String host = awsParameterRetriever.getParameter(hostParam);
	    String dbName = awsParameterRetriever.getParameter(dbNameParam);
	    String serviceName = awsParameterRetriever.getParameter(serviceNameParam);
	    String servicePassword = awsParameterRetriever.getParameter(servicePasswordParam);

	    return new MongoConnection(
	    		clusterName, 
	    		host, 
	    		dbName, 
	    		servicePassword, 
	    		serviceName
	    		);
	}
	
	@Configuration
	public static class MongoConfig extends AbstractMongoClientConfiguration {

		private final DatabaseConnection mongoConnection;

		public MongoConfig(DatabaseConnection mongoConnection) {
			super();
			this.mongoConnection = mongoConnection;
		}
		
		@Override
		public MongoCustomConversions customConversions() {
			return new MongoCustomConversions(
				List.of(
					new ZonedDateTimeReadConverter(),
					new ZonedDateTimeWriteConverter(),
					new TagReadConverter(),
					new TagWriteConverter()
				)
			);
		}

		@Override
		protected String getDatabaseName() {
			if (mongoConnection instanceof MongoConnection mongoconnection) {
				return mongoconnection.dbName();
			}
			throw new IllegalStateException(
					"Unexpected state. Given DatabaseConnection was not of type MongoConnection.");
		}
		
		@Override
		public MongoClient mongoClient() {
			return MongoClients.create(mongoConnection.getConnectionUri());
		}

	}
}
