package com.nomad.backend;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		String profile = System.getProperty("spring.profiles.active", "local");
		if (!Objects.equals(profile, "local")) {
			// Note; the application insights connection string is set as `APPLICATIONINSIGHTS_CONNECTION_STRING` environment variable.
			ApplicationInsights.attach();
		}
		SpringApplication.run(BackendApplication.class, args);
	}

}
