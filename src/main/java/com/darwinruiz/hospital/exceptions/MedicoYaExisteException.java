package com.darwinruiz.hospital.exceptions;

public class MedicoYaExisteException extends RuntimeException {
    
    public MedicoYaExisteException(String colegiado) {
        super("Ya existe un médico registrado con el número de colegiado: " + colegiado);
    }
    
    public MedicoYaExisteException(String message, Throwable cause) {
        super(message, cause);
    }
}