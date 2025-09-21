package com.app.playerservicejava.controller;

import com.app.playerservicejava.controller.users.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    @Autowired
    private UserController controller;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    private String baseUrl() {
        return baseUrl("");
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + "/v1/users" + path;
    }

    @Test
    public void contextLoads() {
        assertThat(controller).isNotNull();
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    public void createUser_createsAndReturnsAUser() {
        String fullName = "FName";
        String email = "email@host.com";
        int age = 24;
        String address = "Address";
        CreateUserRequest request = new CreateUserRequest(fullName, email, age, address);

        ResponseEntity<User> response = restTemplate.postForEntity(baseUrl(), request, User.class);
        User user = response.getBody();
        assertThat(user).isNotNull();
        assertThat(user.getUserId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("email@host.com");
        assertThat(user.getFullName()).isEqualTo("FName");
        assertThat(user.getAge()).isEqualTo(24);
        assertThat(user.getAddress()).isEqualTo("Address");

        ResponseEntity<Users> usersResponse = restTemplate.getForEntity(baseUrl(), Users.class);

        usersResponse.getStatusCode().is2xxSuccessful();
        assertThat(usersResponse.getBody()).isNotNull();
        assertThat(usersResponse.getBody().getUsers()).hasSize(1);
    }

    @Test
    public void createUser_throwsWhenEmailAlreadyExists() {
        String fullName = "FName";
        String email = "email@host.com";
        int age = 24;
        String address = "Address";
        CreateUserRequest request1 = new CreateUserRequest(fullName, email, age, address);
        CreateUserRequest request2 = new CreateUserRequest(fullName, email, age, address);

        ResponseEntity<User> response1 = restTemplate.postForEntity(baseUrl(), request1, User.class);
        ResponseEntity<User> response2 = restTemplate.postForEntity(baseUrl(), request2, User.class);
        User user1 = response1.getBody();
        assertThat(user1).isNotNull();

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<Users> usersResponse = restTemplate.getForEntity(baseUrl(), Users.class);
        usersResponse.getStatusCode().is2xxSuccessful();
        assertThat(usersResponse.getBody()).isNotNull();
        assertThat(usersResponse.getBody().getUsers()).hasSize(1);
    }

    @Test
    public void getUsers_returnsEmptyList_whenNoUsersArePresent() {
        ResponseEntity<Users> emptyUsers = restTemplate.getForEntity(baseUrl(), Users.class);
        emptyUsers.getStatusCode().is2xxSuccessful();
        assertThat(emptyUsers.getBody()).isNotNull();
        assertThat(emptyUsers.getBody().getUsers()).hasSize(0);

        String fullName = "FName";
        String email = "email@host.com";
        int age = 24;
        String address = "Address";
        CreateUserRequest request = new CreateUserRequest(fullName, email, age, address);

        ResponseEntity<User> user = restTemplate.postForEntity(baseUrl(), request, User.class);
        assertTrue(user.getStatusCode().is2xxSuccessful());

        ResponseEntity<Users> users = restTemplate.getForEntity(baseUrl(), Users.class);
        users.getStatusCode().is2xxSuccessful();
        assertThat(users.getBody()).isNotNull();
        assertThat(users.getBody().getUsers()).hasSize(1);
    }
}