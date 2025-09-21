package com.app.playerservicejava.controller;

import com.app.playerservicejava.config.PSErrorResponse;
import com.app.playerservicejava.controller.users.CreateUserRequest;
import com.app.playerservicejava.controller.users.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GlobalExceptionHandlerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    private String baseUrl() {
        return "http://localhost:" + port + "/v1/users";
    }

    @Test
    public void createUser_returns500_whenPSDatabaseExceptionThrown() {
        String fullName = "FName";
        String email = "email@host.com";
        int age = 24;
        String address = "Address";
        CreateUserRequest request = new CreateUserRequest(fullName, email, age, address);

        // Mock the repository to throw an exception during save
        when(userRepository.save(any()))
                .thenThrow(new RuntimeException("Database connection failed"));

        ResponseEntity<PSErrorResponse> response = restTemplate.postForEntity(baseUrl(), request, PSErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("Database error");
        assertThat(response.getBody().message()).isEqualTo("Failed to create user");
    }

    @Test
    public void getUsers_returns500_whenPSDatabaseExceptionThrown() {
        // Mock the repository to throw an exception during findAll
        when(userRepository.findAll())
                .thenThrow(new RuntimeException("Database connection failed"));

        ResponseEntity<PSErrorResponse> response = restTemplate.getForEntity(baseUrl(), PSErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("Database error");
        assertThat(response.getBody().message()).isEqualTo("Failed to get users");
    }
}
