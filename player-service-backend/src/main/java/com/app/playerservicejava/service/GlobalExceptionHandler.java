package com.app.playerservicejava.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {

    @ExceptionHandler(value = PSDuplicateEmailException.class)
    public ResponseEntity<PSErrorResponse> duplicateEmailExceptionHandler(PSDuplicateEmailException e) throws Exception {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new PSErrorResponse(e.getMessage()));
    }
}

