package com.nomad.backend;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		// Note; the application insights connection string is set as an `APPLICATIONINSIGHTS_CONNECTION_STRING` environment variable.
		ApplicationInsights.attach();
		SpringApplication.run(BackendApplication.class, args);
	}

}
