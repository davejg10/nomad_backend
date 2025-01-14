package com.nomad.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
//		System.setProperty("applicationinsights.runtime-attach.configuration.classpath.file", "applicationinsights-dev.json");
//		ApplicationInsights.attach();
//		ConnectionString.configure("<Your Connection String>");
		SpringApplication.run(BackendApplication.class, args);
	}

}
