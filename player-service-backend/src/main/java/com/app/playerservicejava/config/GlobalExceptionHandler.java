package com.app.playerservicejava.config;

import com.app.playerservicejava.controller.users.DuplicateEmailException;
import com.app.playerservicejava.controller.users.PSDatabaseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({DuplicateEmailException.class})
    public ResponseEntity<PSErrorResponse> handleDuplicateEmailException(DuplicateEmailException exception) {
        PSErrorResponse body = new PSErrorResponse("Duplicate email", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }

    @ExceptionHandler({PSDatabaseException.class})
    public ResponseEntity<PSErrorResponse> handlePSDatabaseException(PSDatabaseException exception) {
        PSErrorResponse body = new PSErrorResponse("Database error", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}