package com.aibert.dosw.entrypoints.advice;

import com.aibert.dosw.domain.exceptions.ConnectionRequestException;
import com.aibert.dosw.domain.exceptions.IncompleteProfileException;
import com.aibert.dosw.domain.exceptions.InvalidInvitationException;
import com.aibert.dosw.domain.exceptions.SessionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst().orElse("Datos inválidos");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(SessionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidInvitationException.class)
    public ResponseEntity<Map<String, String>> handleInvalidInvitation(InvalidInvitationException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ConnectionRequestException.class)
    public ResponseEntity<Map<String, String>> handleConnectionRequest(ConnectionRequestException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IncompleteProfileException.class)
    public ResponseEntity<Map<String, String>> handleIncompleteProfile(IncompleteProfileException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }
}
