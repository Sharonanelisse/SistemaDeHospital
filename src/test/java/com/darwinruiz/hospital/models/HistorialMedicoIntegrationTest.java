package com.darwinruiz.hospital.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para verificar la relación OneToOne entre HistorialMedico y Paciente
 */
class HistorialMedicoIntegrationTest {

    private Paciente paciente;
    private HistorialMedico historialMedico;

    @BeforeEach
    void setUp() {
        paciente = new Paciente(
            "Ana López",
            "5555555555555",
            LocalDate.of(1988, 3, 10),
            "55555555",
            "ana.lopez@email.com"
        );
    }

    @Test
    @DisplayName("Debe establecer relación OneToOne bidireccional correctamente")
    void debeEstablecerRelacionOneToOneBidireccional() {
        // Crear historial médico para el paciente
        historialMedico = new HistorialMedico(
            paciente,
            "Alergia a mariscos",
            "Diabetes tipo 2 familiar",
            "Paciente muy colaborador en el tratamiento"
        );
        
        // Establecer relación bidireccional
        paciente.setHistorialMedico(historialMedico);
        
        // Verificar que la relación se estableció correctamente
        assertEquals(paciente, historialMedico.getPaciente());
        assertEquals(historialMedico, paciente.getHistorialMedico());
        
        // Verificar que los datos se mantienen
        assertEquals("Alergia a mariscos", historialMedico.getAlergias());
        assertEquals("Diabetes tipo 2 familiar", historialMedico.getAntecedentes());
        assertEquals("Paciente muy colaborador en el tratamiento", historialMedico.getObservaciones());
    }

    @Test
    @DisplayName("Debe simular comportamiento de @MapsId con IDs compartidos")
    void debeSimularComportamientoMapsIdConIdsCompartidos() {
        // Simular que el paciente fue persistido y tiene un ID
        paciente.setId(100L);
        
        // Crear historial médico
        historialMedico = new HistorialMedico(paciente);
        
        // Simular que @MapsId asigna el mismo ID del paciente al historial
        historialMedico.setId(paciente.getId());
        
        // Verificar que ambos tienen el mismo ID
        assertEquals(paciente.getId(), historialMedico.getId());
        assertEquals(100L, historialMedico.getId());
        
        // Establecer relación bidireccional
        paciente.setHistorialMedico(historialMedico);
        
        // Verificar integridad de la relación
        assertEquals(paciente, historialMedico.getPaciente());
        assertEquals(historialMedico, paciente.getHistorialMedico());
    }

    @Test
    @DisplayName("Debe manejar actualización de historial médico existente")
    void debeManejarActualizacionHistorialMedicoExistente() {
        // Crear historial inicial
        historialMedico = new HistorialMedico(paciente);
        paciente.setHistorialMedico(historialMedico);
        
        // Verificar que inicialmente no tiene información
        assertFalse(historialMedico.tieneInformacion());
        
        // Actualizar información del historial
        historialMedico.actualizarInformacion(
            "Penicilina, Sulfa",
            "Hipertensión materna, Cardiopatía paterna",
            "Paciente ansioso, requiere explicaciones detalladas"
        );
        
        // Verificar que ahora tiene información
        assertTrue(historialMedico.tieneInformacion());
        assertEquals("Penicilina, Sulfa", historialMedico.getAlergias());
        assertEquals("Hipertensión materna, Cardiopatía paterna", historialMedico.getAntecedentes());
        assertEquals("Paciente ansioso, requiere explicaciones detalladas", historialMedico.getObservaciones());
        
        // Verificar que la relación se mantiene
        assertEquals(paciente, historialMedico.getPaciente());
        assertEquals(historialMedico, paciente.getHistorialMedico());
    }

    @Test
    @DisplayName("Debe manejar cascada de eliminación simulada")
    void debeManejarCascadaEliminacionSimulada() {
        // Crear historial médico
        historialMedico = new HistorialMedico(
            paciente,
            "Sin alergias conocidas",
            "Saludable",
            "Paciente activo"
        );
        
        paciente.setHistorialMedico(historialMedico);
        
        // Verificar relación establecida
        assertNotNull(paciente.getHistorialMedico());
        assertEquals(paciente, historialMedico.getPaciente());
        
        // Simular eliminación del historial del paciente (cascade)
        paciente.setHistorialMedico(null);
        
        // El historial aún mantiene la referencia al paciente
        // (en JPA real, esto se manejaría automáticamente con cascade)
        assertEquals(paciente, historialMedico.getPaciente());
    }

    @Test
    @DisplayName("Debe validar integridad referencial")
    void debeValidarIntegridadReferencial() {
        // Crear historial médico
        historialMedico = new HistorialMedico(paciente);
        paciente.setHistorialMedico(historialMedico);
        
        // Intentar cambiar el paciente del historial a null debe fallar
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> historialMedico.setPaciente(null)
        );
        
        assertEquals("El paciente no puede ser nulo", exception.getMessage());
        
        // La relación original debe mantenerse intacta
        assertEquals(paciente, historialMedico.getPaciente());
        assertEquals(historialMedico, paciente.getHistorialMedico());
    }

    @Test
    @DisplayName("Debe permitir cambio de paciente en historial médico")
    void debePermitirCambioPacienteEnHistorialMedico() {
        // Crear historial médico inicial
        historialMedico = new HistorialMedico(paciente, "Alergia inicial", "Antecedente inicial", "Observación inicial");
        paciente.setHistorialMedico(historialMedico);
        
        // Crear nuevo paciente
        Paciente nuevoPaciente = new Paciente(
            "Carlos Ruiz",
            "7777777777777",
            LocalDate.of(1992, 7, 25),
            "77777777",
            "carlos.ruiz@email.com"
        );
        
        // Cambiar el paciente del historial
        historialMedico.setPaciente(nuevoPaciente);
        
        // Verificar que el cambio se realizó correctamente
        assertEquals(nuevoPaciente, historialMedico.getPaciente());
        
        // El historial mantiene su información
        assertEquals("Alergia inicial", historialMedico.getAlergias());
        assertEquals("Antecedente inicial", historialMedico.getAntecedentes());
        assertEquals("Observación inicial", historialMedico.getObservaciones());
        
        // Establecer relación bidireccional con el nuevo paciente
        nuevoPaciente.setHistorialMedico(historialMedico);
        assertEquals(historialMedico, nuevoPaciente.getHistorialMedico());
    }

    @Test
    @DisplayName("Debe manejar múltiples actualizaciones de información")
    void debeManejarMultiplesActualizacionesInformacion() {
        historialMedico = new HistorialMedico(paciente);
        paciente.setHistorialMedico(historialMedico);
        
        // Primera actualización
        historialMedico.actualizarInformacion("Alergia A", "Antecedente A", "Observación A");
        assertTrue(historialMedico.tieneInformacion());
        
        // Segunda actualización
        historialMedico.actualizarInformacion("Alergia B", "Antecedente B", "Observación B");
        assertEquals("Alergia B", historialMedico.getAlergias());
        assertEquals("Antecedente B", historialMedico.getAntecedentes());
        assertEquals("Observación B", historialMedico.getObservaciones());
        
        // Tercera actualización con campos nulos
        historialMedico.actualizarInformacion(null, null, "Solo observación");
        assertNull(historialMedico.getAlergias());
        assertNull(historialMedico.getAntecedentes());
        assertEquals("Solo observación", historialMedico.getObservaciones());
        assertTrue(historialMedico.tieneInformacion()); // Aún tiene información en observaciones
        
        // Cuarta actualización limpiando todo
        historialMedico.actualizarInformacion("", "   ", null);
        assertFalse(historialMedico.tieneInformacion());
        
        // La relación con el paciente se mantiene
        assertEquals(paciente, historialMedico.getPaciente());
        assertEquals(historialMedico, paciente.getHistorialMedico());
    }
}