package com.darwinruiz.hospital.exceptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FechaInvalidaException extends RuntimeException {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public FechaInvalidaException(LocalDateTime fechaHora) {
        super("La fecha y hora " + fechaHora.format(FORMATTER) + " no es válida. Las citas deben programarse para fechas futuras.");
    }
    
    public FechaInvalidaException(String message) {
        super(message);
    }
    
    public FechaInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}