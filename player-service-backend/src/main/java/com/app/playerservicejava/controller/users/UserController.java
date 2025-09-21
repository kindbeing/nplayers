package com.app.playerservicejava.controller.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "v1/users", produces = { MediaType.APPLICATION_JSON_VALUE })
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Users> getUsers() {
        List<User> userList = userService.getUsers();
        Users users = new Users();
        users.setUsers(userList);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }
}

