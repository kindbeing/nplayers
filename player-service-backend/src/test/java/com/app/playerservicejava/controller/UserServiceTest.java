package com.app.playerservicejava.controller;

import com.app.playerservicejava.controller.users.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void getUsers_throwsWhenDatabaseError() {
        // Arrange
        when(userRepository.findAll())
                .thenThrow(new DataAccessResourceFailureException("Access failed"));

        // Act & Assert
        PSDatabaseException exception = assertThrows(PSDatabaseException.class, () -> {
            userService.getUsers();
        });

        assertThat(exception.getMessage()).contains("Failed to get users");
    }

    @Test
    public void createUser_whenFindByEmailFails_throwsKnownException() {
        // Arrange
        when(userRepository.findByEmail(any(String.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));

        // Act & Assert
        PSDatabaseException exception = assertThrows(PSDatabaseException.class, () -> {
            userService.createUser(new CreateUserRequest("test", "email", 25, "addr"));
        });

        assertThat(exception.getMessage()).contains("Failed to find a user by email");
    }

    @Test
    public void createUser_whenSaveFails_throwsKnownException() {
        // Arrange
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));

        // Act & Assert
        PSDatabaseException exception = assertThrows(PSDatabaseException.class, () -> {
            userService.createUser(new CreateUserRequest("test", "email", 25, "addr"));
        });

        assertThat(exception.getMessage()).contains("Failed to create user");
    }
}