package com.darwinruiz.hospital.exceptions;

public class EmailInvalidoException extends RuntimeException {
    
    public EmailInvalidoException(String email) {
        super("El formato del email '" + email + "' no es válido");
    }
    
    public EmailInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}