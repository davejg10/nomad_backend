package com.nomad.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class BackendApplicationTests {

    @MockitoBean
    private CommandLineRunner loadDatabase; // Dont include this bean as it requires connection to DB

	@Test
	void contextLoads() {
	}

}
