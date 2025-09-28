package com.darwinruiz.hospital.exceptions;

public class PacienteYaExisteException extends RuntimeException {
    
    public PacienteYaExisteException(String dpi) {
        super("Ya existe un paciente registrado con el DPI: " + dpi);
    }
    
    public PacienteYaExisteException(String message, Throwable cause) {
        super(message, cause);
    }
}