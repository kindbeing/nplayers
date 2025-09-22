package com.app.playerservicejava.service;

public class PSDuplicateEmailException extends RuntimeException {
    public PSDuplicateEmailException(String message) {
        super(message);
    }
}
