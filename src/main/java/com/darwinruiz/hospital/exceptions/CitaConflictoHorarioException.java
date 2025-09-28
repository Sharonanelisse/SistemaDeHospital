package com.darwinruiz.hospital.exceptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CitaConflictoHorarioException extends RuntimeException {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public CitaConflictoHorarioException(String nombreMedico, LocalDateTime fechaHora) {
        super("El médico " + nombreMedico + " ya tiene una cita programada para el " + 
              fechaHora.format(FORMATTER));
    }
    
    public CitaConflictoHorarioException(String message) {
        super(message);
    }
    
    public CitaConflictoHorarioException(String message, Throwable cause) {
        super(message, cause);
    }
}