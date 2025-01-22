package com.nomad.backend;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

//    @Test
//    public void testGreetingEndpoint() throws Exception {
//        MvcResult result = mockMvc.perform(get("/helloworld?name=John"))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        String responseContent = result.getResponse().getContentAsString();
//
//        // This assertion will fail because the actual response is "Hello John"
//        assertThat(responseContent).isEqualTo("HelloJohn");
//    }
}
