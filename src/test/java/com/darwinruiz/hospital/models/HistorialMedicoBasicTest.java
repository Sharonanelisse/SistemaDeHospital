package com.darwinruiz.hospital.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests básicos para verificar que HistorialMedico compila y funciona correctamente
 */
class HistorialMedicoBasicTest {

    @Test
    @DisplayName("Debe crear instancia de HistorialMedico sin errores")
    void debeCrearInstanciaHistorialMedicoSinErrores() {
        // Crear paciente básico
        Paciente paciente = new Paciente();
        paciente.setNombre("Test Paciente");
        paciente.setDpi("1234567890123");
        paciente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        paciente.setEmail("test@email.com");
        
        // Crear historial médico
        HistorialMedico historial = new HistorialMedico(paciente);
        
        // Verificaciones básicas
        assertNotNull(historial);
        assertEquals(paciente, historial.getPaciente());
        assertNull(historial.getAlergias());
        assertNull(historial.getAntecedentes());
        assertNull(historial.getObservaciones());
    }

    @Test
    @DisplayName("Debe manejar anotaciones JPA correctamente")
    void debeManejarAnotacionesJPACorrectamente() {
        // Verificar que la clase tiene las anotaciones necesarias
        assertTrue(HistorialMedico.class.isAnnotationPresent(jakarta.persistence.Entity.class));
        assertTrue(HistorialMedico.class.isAnnotationPresent(jakarta.persistence.Table.class));
        
        // Verificar nombre de tabla
        jakarta.persistence.Table tableAnnotation = HistorialMedico.class.getAnnotation(jakarta.persistence.Table.class);
        assertEquals("historial_medico", tableAnnotation.name());
    }

    @Test
    @DisplayName("Debe tener configuración correcta de @MapsId")
    void debeTenerConfiguracionCorrectaMapsId() throws NoSuchFieldException {
        // Verificar que el campo paciente tiene @MapsId
        var pacienteField = HistorialMedico.class.getDeclaredField("paciente");
        assertTrue(pacienteField.isAnnotationPresent(jakarta.persistence.MapsId.class));
        assertTrue(pacienteField.isAnnotationPresent(jakarta.persistence.OneToOne.class));
        assertTrue(pacienteField.isAnnotationPresent(jakarta.persistence.JoinColumn.class));
        
        // Verificar configuración de @JoinColumn
        jakarta.persistence.JoinColumn joinColumn = pacienteField.getAnnotation(jakarta.persistence.JoinColumn.class);
        assertEquals("paciente_id", joinColumn.name());
        
        // Verificar configuración de @ForeignKey
        jakarta.persistence.ForeignKey foreignKey = joinColumn.foreignKey();
        assertEquals("fk_historial_paciente", foreignKey.name());
    }

    @Test
    @DisplayName("Debe validar longitudes de campos correctamente")
    void debeValidarLongitudesCamposCorrectamente() throws NoSuchFieldException {
        // Verificar anotaciones @Column en los campos
        var alergiasField = HistorialMedico.class.getDeclaredField("alergias");
        jakarta.persistence.Column alergiasColumn = alergiasField.getAnnotation(jakarta.persistence.Column.class);
        assertEquals(500, alergiasColumn.length());
        
        var antecedentesField = HistorialMedico.class.getDeclaredField("antecedentes");
        jakarta.persistence.Column antecedentesColumn = antecedentesField.getAnnotation(jakarta.persistence.Column.class);
        assertEquals(1000, antecedentesColumn.length());
        
        var observacionesField = HistorialMedico.class.getDeclaredField("observaciones");
        jakarta.persistence.Column observacionesColumn = observacionesField.getAnnotation(jakarta.persistence.Column.class);
        assertEquals(1000, observacionesColumn.length());
    }

    @Test
    @DisplayName("Debe funcionar método tieneInformacion correctamente")
    void debeFuncionarMetodoTieneInformacionCorrectamente() {
        Paciente paciente = new Paciente();
        paciente.setNombre("Test");
        paciente.setDpi("1234567890123");
        paciente.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        paciente.setEmail("test@email.com");
        
        HistorialMedico historial = new HistorialMedico(paciente);
        
        // Sin información
        assertFalse(historial.tieneInformacion());
        
        // Con alergias
        historial.setAlergias("Penicilina");
        assertTrue(historial.tieneInformacion());
        
        // Limpiar y probar antecedentes
        historial.setAlergias(null);
        historial.setAntecedentes("Diabetes");
        assertTrue(historial.tieneInformacion());
        
        // Limpiar y probar observaciones
        historial.setAntecedentes(null);
        historial.setObservaciones("Paciente colaborador");
        assertTrue(historial.tieneInformacion());
    }
}