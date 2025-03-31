package com.nomad.backend;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.core.env.Environment;

import java.util.Objects;

@Log4j2
@SpringBootApplication
@EntityScan(basePackages = {"com.nomad.data_library.domain"})
public class BackendApplication {

	public static void main(String[] args) {
		Environment environment = SpringApplication.run(BackendApplication.class, args).getEnvironment();

		String profile = environment.getProperty("spring.profiles.active", "local");
		
		log.info("Profile is: {}", profile);

		if (!Objects.equals(profile, "local")) {
			log.info("Attaching application insights");
			// Note; the application insights connection string is set as `APPLICATIONINSIGHTS_CONNECTION_STRING` environment variable.
			ApplicationInsights.attach();
		}
	}

}
