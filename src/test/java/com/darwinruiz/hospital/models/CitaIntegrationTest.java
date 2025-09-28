package com.darwinruiz.hospital.models;

import com.darwinruiz.hospital.enums.EstadoCita;
import com.darwinruiz.hospital.enums.Especialidad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para verificar que la entidad Cita funciona correctamente
 * con las entidades Paciente y Medico existentes.
 */
@DisplayName("Cita - Tests de Integración")
class CitaIntegrationTest {

    private Paciente paciente;
    private Medico medico;
    private LocalDateTime fechaFutura;

    @BeforeEach
    void setUp() {
        // Crear paciente usando el constructor existente
        paciente = new Paciente(
            "Juan Pérez",
            "1234567890123",
            LocalDate.of(1990, 1, 1),
            "12345678",
            "juan@email.com"
        );
        paciente.setId(1L);

        // Crear médico usando el constructor existente
        medico = new Medico(
            "Dr. García",
            "COL123",
            Especialidad.CARDIOLOGIA,
            "garcia@hospital.com"
        );
        medico.setId(1L);

        fechaFutura = LocalDateTime.now().plusDays(1);
    }

    @Test
    @DisplayName("Debe integrar correctamente con Paciente existente")
    void debeIntegrarCorrectamenteConPacienteExistente() {
        // When
        Cita cita = new Cita(fechaFutura, "Consulta cardiológica", paciente, medico);

        // Then
        assertNotNull(cita.getPaciente());
        assertEquals("Juan Pérez", cita.getPaciente().getNombre());
        assertEquals("1234567890123", cita.getPaciente().getDpi());
        assertEquals("juan@email.com", cita.getPaciente().getEmail());
        
        // Verificar que la relación bidireccional funciona
        paciente.agregarCita(cita);
        assertTrue(paciente.getCitas().contains(cita));
    }

    @Test
    @DisplayName("Debe integrar correctamente con Medico existente")
    void debeIntegrarCorrectamenteConMedicoExistente() {
        // When
        Cita cita = new Cita(fechaFutura, "Consulta cardiológica", paciente, medico);

        // Then
        assertNotNull(cita.getMedico());
        assertEquals("Dr. García", cita.getMedico().getNombre());
        assertEquals("COL123", cita.getMedico().getColegiado());
        assertEquals(Especialidad.CARDIOLOGIA, cita.getMedico().getEspecialidad());
        assertEquals("garcia@hospital.com", cita.getMedico().getEmail());
        
        // Verificar que la relación bidireccional funciona
        medico.addCita(cita);
        assertTrue(medico.getCitas().contains(cita));
    }

    @Test
    @DisplayName("Debe mantener consistencia en relaciones bidireccionales")
    void debeMantenerConsistenciaEnRelacionesBidireccionales() {
        // Given
        Cita cita = new Cita(fechaFutura, "Consulta", paciente, medico);

        // When - Agregar cita al paciente
        paciente.agregarCita(cita);
        
        // Then - Verificar consistencia
        assertTrue(paciente.getCitas().contains(cita));
        assertEquals(paciente, cita.getPaciente());

        // When - Agregar cita al médico
        medico.addCita(cita);
        
        // Then - Verificar consistencia
        assertTrue(medico.getCitas().contains(cita));
        assertEquals(medico, cita.getMedico());
    }

    @Test
    @DisplayName("Debe permitir múltiples citas para el mismo paciente")
    void debePermitirMultiplesCitasParaElMismoPaciente() {
        // Given
        LocalDateTime fecha1 = fechaFutura;
        LocalDateTime fecha2 = fechaFutura.plusDays(1);
        
        Cita cita1 = new Cita(fecha1, "Primera consulta", paciente, medico);
        Cita cita2 = new Cita(fecha2, "Segunda consulta", paciente, medico);

        // When
        paciente.agregarCita(cita1);
        paciente.agregarCita(cita2);

        // Then
        assertEquals(2, paciente.getCitas().size());
        assertTrue(paciente.getCitas().contains(cita1));
        assertTrue(paciente.getCitas().contains(cita2));
        assertEquals(paciente, cita1.getPaciente());
        assertEquals(paciente, cita2.getPaciente());
    }

    @Test
    @DisplayName("Debe permitir múltiples citas para el mismo médico")
    void debePermitirMultiplesCitasParaElMismoMedico() {
        // Given
        Paciente otroPaciente = new Paciente(
            "María García",
            "9876543210987",
            LocalDate.of(1985, 5, 15),
            "87654321",
            "maria@email.com"
        );
        otroPaciente.setId(2L);

        LocalDateTime fecha1 = fechaFutura;
        LocalDateTime fecha2 = fechaFutura.plusHours(2);
        
        Cita cita1 = new Cita(fecha1, "Consulta paciente 1", paciente, medico);
        Cita cita2 = new Cita(fecha2, "Consulta paciente 2", otroPaciente, medico);

        // When
        medico.addCita(cita1);
        medico.addCita(cita2);

        // Then
        assertEquals(2, medico.getCitas().size());
        assertTrue(medico.getCitas().contains(cita1));
        assertTrue(medico.getCitas().contains(cita2));
        assertEquals(medico, cita1.getMedico());
        assertEquals(medico, cita2.getMedico());
    }

    @Test
    @DisplayName("Debe remover citas correctamente de las relaciones")
    void debeRemoverCitasCorrectamenteDeLasRelaciones() {
        // Given
        Cita cita = new Cita(fechaFutura, "Consulta", paciente, medico);
        paciente.agregarCita(cita);
        medico.addCita(cita);

        // Verificar estado inicial
        assertTrue(paciente.getCitas().contains(cita));
        assertTrue(medico.getCitas().contains(cita));

        // When - Remover de paciente
        paciente.removerCita(cita);

        // Then
        assertFalse(paciente.getCitas().contains(cita));
        assertNull(cita.getPaciente());

        // When - Remover de médico
        medico.removeCita(cita);

        // Then
        assertFalse(medico.getCitas().contains(cita));
        assertNull(cita.getMedico());
    }

    @Test
    @DisplayName("Debe validar datos con entidades relacionadas reales")
    void debeValidarDatosConEntidadesRelacionadasReales() {
        // Given - Crear cita con entidades válidas
        Cita cita = new Cita(fechaFutura, "Consulta de seguimiento", paciente, medico);

        // When & Then - No debe lanzar excepciones
        assertDoesNotThrow(() -> cita.validarDatos());
        
        // Verificar que todos los datos están correctos
        assertEquals(fechaFutura, cita.getFechaHora());
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
        assertEquals("Consulta de seguimiento", cita.getMotivo());
        assertEquals(paciente, cita.getPaciente());
        assertEquals(medico, cita.getMedico());
    }

    @Test
    @DisplayName("Debe detectar conflictos de horario con entidades reales")
    void debeDetectarConflictosDeHorarioConEntidadesReales() {
        // Given
        Paciente otroPaciente = new Paciente(
            "María García",
            "9876543210987",
            LocalDate.of(1985, 5, 15),
            "87654321",
            "maria@email.com"
        );

        LocalDateTime mismaFecha = fechaFutura;
        
        Cita cita1 = new Cita(mismaFecha, "Consulta 1", paciente, medico);
        Cita cita2 = new Cita(mismaFecha, "Consulta 2", otroPaciente, medico);

        // When & Then
        assertTrue(cita1.conflictaConHorario(cita2));
        assertTrue(cita2.conflictaConHorario(cita1));
    }

    @Test
    @DisplayName("Debe cambiar estados correctamente con entidades relacionadas")
    void debeCambiarEstadosCorrectamenteConEntidadesRelacionadas() {
        // Given
        Cita cita = new Cita(fechaFutura, "Consulta", paciente, medico);
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());

        // When - Cambiar a ATENDIDA
        cita.cambiarEstado(EstadoCita.ATENDIDA);

        // Then
        assertEquals(EstadoCita.ATENDIDA, cita.getEstado());
        
        // Verificar que las relaciones se mantienen
        assertEquals(paciente, cita.getPaciente());
        assertEquals(medico, cita.getMedico());
    }

    @Test
    @DisplayName("Debe generar toString con información de entidades relacionadas")
    void debeGenerarToStringConInformacionDeEntidadesRelacionadas() {
        // Given
        Cita cita = new Cita(fechaFutura, "Consulta cardiológica", paciente, medico);
        cita.setId(1L);

        // When
        String toString = cita.toString();

        // Then
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("estado=PROGRAMADA"));
        assertTrue(toString.contains("motivo='Consulta cardiológica'"));
        assertTrue(toString.contains("paciente=Juan Pérez"));
        assertTrue(toString.contains("medico=Dr. García"));
    }

    @Test
    @DisplayName("Debe implementar equals correctamente con entidades relacionadas")
    void debeImplementarEqualsCorrectamenteConEntidadesRelacionadas() {
        // Given
        LocalDateTime mismaFecha = fechaFutura;
        
        Cita cita1 = new Cita(mismaFecha, "Motivo 1", paciente, medico);
        Cita cita2 = new Cita(mismaFecha, "Motivo 2", paciente, medico);

        // When & Then
        assertEquals(cita1, cita2); // Misma fecha, mismo paciente, mismo médico
        assertEquals(cita1.hashCode(), cita2.hashCode());
    }
}