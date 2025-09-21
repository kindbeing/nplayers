package com.app.playerservicejava;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PlayerServiceJavaApplicationTests {

    @Test
    void contextLoads() {
    }

}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpRequestTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void greetingShouldReturnDefaultMessage() throws Exception {
        // Build the URL
        String url = "http://localhost:" + port + "/";

        // Make the HTTP request
        String response = this.restTemplate.getForObject(url, String.class);

        // Check if response contains expected content
        boolean containsTemplated = response.contains("\"templated\" : true");

        // Assert the result
        assertThat(containsTemplated).isTrue();
    }
}



@SpringBootTest
@AutoConfigureMockMvc
class TestingWebApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnDefaultMessage() throws Exception {
        // Prepare the request
        MockHttpServletRequestBuilder request = get("/");

        // Perform the request
        ResultActions result = this.mockMvc.perform(request);

        // Print the result for debugging
        result.andDo(print());

        // Check the status
        result.andExpect(status().isOk());

        // Verify the content contains expected string
        result.andExpect(content().string(containsString("http://localhost/players{?page,size,sort*}")));
    }
}