package com.nomad.backend;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Objects;

@Log4j2
@SpringBootApplication
@EntityScan(basePackages = {"com.nomad.data_library.domain"})
@EnableJpaRepositories({"com.nomad.data_library.repositories", "com.nomad.backend.routes"})
@ComponentScan({"com.nomad.data_library.config", "com.nomad.backend"})
public class BackendApplication {

	public static void main(String[] args) {
		Environment environment = SpringApplication.run(BackendApplication.class, args).getEnvironment();

		String profile = environment.getProperty("spring.profiles.active", "local");

		log.info("Spring profile is {}", profile);

		if (!Objects.equals(profile, "local")) {
			log.info("Attaching application insights");
			// Note; the application insights connection string is set as `APPLICATIONINSIGHTS_CONNECTION_STRING` environment variable.
			ApplicationInsights.attach();
		}
	}

}
