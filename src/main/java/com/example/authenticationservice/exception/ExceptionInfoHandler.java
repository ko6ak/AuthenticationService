package com.example.authenticationservice.exception;

import com.example.authenticationservice.dto.response.MessageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionInfoHandler {

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<?> emailNotFoundException(EmailNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler({TokenException.class, ConstraintViolationException.class, ConfirmEmailException.class})
    public ResponseEntity<?> tokenException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(e.getMessage()));
    }
}
