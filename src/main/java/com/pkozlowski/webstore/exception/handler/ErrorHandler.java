package com.pkozlowski.webstore.exception.handler;

import com.pkozlowski.webstore.exception.ItemException;
import com.pkozlowski.webstore.exception.UserException;
import com.pkozlowski.webstore.exception.response.UserErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<UserErrorResponse> handleNotFound(UserException exc) {
        UserErrorResponse response = UserErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(exc.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<UserErrorResponse> handleNotFound(ItemException exc) {
        UserErrorResponse response = UserErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(exc.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<UserErrorResponse> handleConflict(Exception exc) {
        UserErrorResponse response = UserErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(exc.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
}
