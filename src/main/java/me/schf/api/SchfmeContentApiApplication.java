package me.schf.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SchfmeContentApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchfmeContentApiApplication.class, args);
	}

}
