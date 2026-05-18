package com.aibert.dosw.domain.exceptions;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException() { super("Sesión de estudio no encontrada"); }
}
