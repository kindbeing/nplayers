package com.app.playerservicejava.controller.users;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            throw new PSDatabaseException("Failed to get users");
        }
    }

    public User createUser(CreateUserRequest request) {
        try {
            Optional<User> existingUserWithEmail = userRepository.findByEmail(request.email());
            if (existingUserWithEmail.isPresent()) {
                throw new DuplicateEmailException("Email already exists");
            }
        } catch (Exception e) {
            throw new PSDatabaseException("Failed to find a user by email");
        }

        try {
            User entity = new User(request.email(),
                    request.fullName(),
                    request.age(),
                    request.address());
            return userRepository.save(entity);
        } catch (Exception e) {
            throw new PSDatabaseException("Failed to create user");
        }
    }
}