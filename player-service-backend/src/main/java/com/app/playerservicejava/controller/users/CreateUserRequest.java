package com.app.playerservicejava.controller.users;

public record CreateUserRequest(String fullName, String email, int age, String address) {
}
