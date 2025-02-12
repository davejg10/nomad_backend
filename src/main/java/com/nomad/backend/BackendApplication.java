package com.nomad.backend;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
@Log4j2
public class BackendApplication {

	public static void main(String[] args) {
		String profile = System.getProperty("spring.profiles.active", "local");
		log.info("In main method with Spring profile; {}", profile);

		if (!Objects.equals(profile, "local")) {
			log.info("Attaching application insights");
			// Note; the application insights connection string is set as `APPLICATIONINSIGHTS_CONNECTION_STRING` environment variable.

			ApplicationInsights.attach();
		}
		SpringApplication.run(BackendApplication.class, args);
	}

}
