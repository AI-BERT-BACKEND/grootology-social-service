package com.aibert.dosw.entrypoints.advice;

import com.aibert.dosw.domain.exceptions.ConnectionRequestException;
import com.aibert.dosw.domain.exceptions.IncompleteProfileException;
import com.aibert.dosw.domain.exceptions.InvalidInvitationException;
import com.aibert.dosw.domain.exceptions.SessionNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404() {
        ResponseEntity<Map<String, String>> r = handler.handleNotFound(new SessionNotFoundException());
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
        assertTrue(r.getBody().containsKey("error"));
    }

    @Test
    void handleInvalidInvitation_returns400() {
        ResponseEntity<Map<String, String>> r = handler.handleInvalidInvitation(
                new InvalidInvitationException("error"));
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
    }

    @Test
    void handleConnectionRequest_returns400WithMessage() {
        ResponseEntity<Map<String, String>> r = handler.handleConnectionRequest(
                new ConnectionRequestException("Ya son amigos"));
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
        assertEquals("Ya son amigos", r.getBody().get("error"));
    }

    @Test
    void handleIncompleteProfile_returns403() {
        ResponseEntity<Map<String, String>> r = handler.handleIncompleteProfile(
                new IncompleteProfileException("Perfil incompleto"));
        assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
        assertEquals("Perfil incompleto", r.getBody().get("error"));
    }
}
