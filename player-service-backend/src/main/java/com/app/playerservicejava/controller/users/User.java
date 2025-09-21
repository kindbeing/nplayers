package com.app.playerservicejava.controller.users;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "USERS")
@Getter
public final class User {

    @Id
    @Column(name = "USERID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(name = "EMAIL", unique = true)
    private String email;

    @Column(name = "FULLNAME")
    private String fullName;

    @Column(name = "AGE")
    private int age;

    @Column(name = "ADDRESS")
    private String address;

    public User() {}

    public User(String email, String fullName, int age, String address) {
        this.email = email;
        this.fullName = fullName;
        this.age = age;
        this.address = address;
    }
}
