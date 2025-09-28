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
 * Tests unitarios para la entidad Cita.
 * Verifica validaciones, relaciones ManyToOne y lógica de negocio.
 */
@DisplayName("Cita - Tests Unitarios")
class CitaTest {

    private Paciente paciente;
    private Medico medico;
    private LocalDateTime fechaFutura;
    private LocalDateTime fechaPasada;

    @BeforeEach
    void setUp() {
        // Crear paciente de prueba
        paciente = new Paciente(
            "Juan Pérez",
            "1234567890123",
            LocalDate.of(1990, 1, 1),
            "12345678",
            "juan@email.com"
        );
        paciente.setId(1L);

        // Crear médico de prueba
        medico = new Medico(
            "Dr. García",
            "COL123",
            Especialidad.CARDIOLOGIA,
            "garcia@hospital.com"
        );
        medico.setId(1L);

        // Fechas de prueba
        fechaFutura = LocalDateTime.now().plusDays(1);
        fechaPasada = LocalDateTime.now().minusDays(1);
    }

    @Test
    @DisplayName("Debe crear cita con constructor básico")
    void debeCrearCitaConConstructorBasico() {
        Cita cita = new Cita();
        
        assertNotNull(cita);
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
        assertNull(cita.getId());
        assertNull(cita.getFechaHora());
        assertNull(cita.getMotivo());
        assertNull(cita.getPaciente());
        assertNull(cita.getMedico());
    }

    @Test
    @DisplayName("Debe crear cita con constructor completo")
    void debeCrearCitaConConstructorCompleto() {
        String motivo = "Consulta general";
        
        Cita cita = new Cita(fechaFutura, motivo, paciente, medico);
        
        assertNotNull(cita);
        assertEquals(fechaFutura, cita.getFechaHora());
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
        assertEquals(motivo, cita.getMotivo());
        assertEquals(paciente, cita.getPaciente());
        assertEquals(medico, cita.getMedico());
    }

    @Test
    @DisplayName("Debe crear cita con estado específico")
    void debeCrearCitaConEstadoEspecifico() {
        String motivo = "Consulta de seguimiento";
        
        Cita cita = new Cita(fechaFutura, EstadoCita.ATENDIDA, motivo, paciente, medico);
        
        assertEquals(EstadoCita.ATENDIDA, cita.getEstado());
        assertEquals(fechaFutura, cita.getFechaHora());
        assertEquals(motivo, cita.getMotivo());
        assertEquals(paciente, cita.getPaciente());
        assertEquals(medico, cita.getMedico());
    }

    @Test
    @DisplayName("Debe establecer estado PROGRAMADA por defecto si es null")
    void debeEstablecerEstadoProgramadaPorDefecto() {
        Cita cita = new Cita(fechaFutura, null, "Motivo", paciente, medico);
        
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
    }

    @Test
    @DisplayName("Debe validar fecha futura en constructor")
    void debeValidarFechaFuturaEnConstructor() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Cita(fechaPasada, "Motivo", paciente, medico);
        });
    }

    @Test
    @DisplayName("Debe validar paciente obligatorio en constructor")
    void debeValidarPacienteObligatorioEnConstructor() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Cita(fechaFutura, "Motivo", null, medico);
        });
    }

    @Test
    @DisplayName("Debe validar médico obligatorio en constructor")
    void debeValidarMedicoObligatorioEnConstructor() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Cita(fechaFutura, "Motivo", paciente, null);
        });
    }

    @Test
    @DisplayName("Debe validar fecha obligatoria en constructor")
    void debeValidarFechaObligatoriaEnConstructor() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Cita(null, "Motivo", paciente, medico);
        });
    }

    @Test
    @DisplayName("Debe validar longitud máxima del motivo")
    void debeValidarLongitudMaximaDelMotivo() {
        String motivoLargo = "a".repeat(201); // 201 caracteres
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Cita(fechaFutura, motivoLargo, paciente, medico);
        });
    }

    @Test
    @DisplayName("Debe permitir motivo de 200 caracteres exactos")
    void debePermitirMotivoDe200Caracteres() {
        String motivo200 = "a".repeat(200);
        
        assertDoesNotThrow(() -> {
            Cita cita = new Cita(fechaFutura, motivo200, paciente, medico);
            assertEquals(motivo200, cita.getMotivo());
        });
    }

    @Test
    @DisplayName("Debe permitir motivo null")
    void debePermitirMotivoNull() {
        assertDoesNotThrow(() -> {
            Cita cita = new Cita(fechaFutura, null, paciente, medico);
            assertNull(cita.getMotivo());
        });
    }

    @Test
    @DisplayName("Debe validar fecha futura en setter")
    void debeValidarFechaFuturaEnSetter() {
        Cita cita = new Cita(fechaFutura, "Motivo", paciente, medico);
        
        assertThrows(IllegalArgumentException.class, () -> {
            cita.setFechaHora(fechaPasada);
        });
    }

    @Test
    @DisplayName("Debe validar estado no nulo en setter")
    void debeValidarEstadoNoNuloEnSetter() {
        Cita cita = new Cita(fechaFutura, "Motivo", paciente, medico);
        
        assertThrows(IllegalArgumentException.class, () -> {
            cita.setEstado(null);
        });
    }

    @Test
    @DisplayName("Debe validar motivo en setter")
    void debeValidarMotivoEnSetter() {
        Cita cita = new Cita(fechaFutura, "Motivo", paciente, medico);
        String motivoLargo = "a".repeat(201);
        
        assertThrows(IllegalArgumentException.class, () -> {
            cita.setMotivo(motivoLargo);
        });
    }

    @Test
    @DisplayName("Debe verificar si puede ser cancelada")
    void debeVerificarSiPuedeSerCancelada() {
        Cita citaProgramada = new Cita(fechaFutura, "Motivo", paciente, medico);
        assertTrue(citaProgramada.puedeSerCancelada());
        
        citaProgramada.setEstado(EstadoCita.ATENDIDA);
        assertFalse(citaProgramada.puedeSerCancelada());
        
        citaProgramada.setEstado(EstadoCita.CANCELADA);
        assertFalse(citaProgramada.puedeSerCancelada());
    }

    @Test
    @DisplayName("Debe verificar si puede ser atendida")
    void debeVerificarSiPuedeSerAtendida() {
        Cita citaProgramada = new Cita(fechaFutura, "Motivo", paciente, medico);
        assertTrue(citaProgramada.puedeSerAtendida());
        
        citaProgramada.setEstado(EstadoCita.ATENDIDA);
        assertFalse(citaProgramada.puedeSerAtendida());
        
        citaProgramada.setEstado(EstadoCita.CANCELADA);
        assertFalse(citaProgramada.puedeSerAtendida());
    }

    @Test
    @DisplayName("Debe verificar si es en el futuro")
    void debeVerificarSiEsEnElFuturo() {
        Cita citaFutura = new Cita(fechaFutura, "Motivo", paciente, medico);
        assertTrue(citaFutura.esEnElFuturo());
        
        citaFutura.setFechaHora(LocalDateTime.now().plusMinutes(1));
        assertTrue(citaFutura.esEnElFuturo());
    }

    @Test
    @DisplayName("Debe detectar conflicto de horario")
    void debeDetectarConflictoDeHorario() {
        Cita cita1 = new Cita(fechaFutura, "Motivo 1", paciente, medico);
        Cita cita2 = new Cita(fechaFutura, "Motivo 2", paciente, medico);
        
        assertTrue(cita1.conflictaConHorario(cita2));
        assertTrue(cita2.conflictaConHorario(cita1));
    }

    @Test
    @DisplayName("No debe detectar conflicto con diferente médico")
    void noDebeDetectarConflictoConDiferenteMedico() {
        Medico otroMedico = new Medico("Dr. López", "COL456", Especialidad.PEDIATRIA, "lopez@hospital.com");
        otroMedico.setId(2L);
        
        Cita cita1 = new Cita(fechaFutura, "Motivo 1", paciente, medico);
        Cita cita2 = new Cita(fechaFutura, "Motivo 2", paciente, otroMedico);
        
        assertFalse(cita1.conflictaConHorario(cita2));
    }

    @Test
    @DisplayName("No debe detectar conflicto con diferente horario")
    void noDebeDetectarConflictoConDiferenteHorario() {
        LocalDateTime otraFecha = fechaFutura.plusHours(1);
        
        Cita cita1 = new Cita(fechaFutura, "Motivo 1", paciente, medico);
        Cita cita2 = new Cita(otraFecha, "Motivo 2", paciente, medico);
        
        assertFalse(cita1.conflictaConHorario(cita2));
    }

    @Test
    @DisplayName("Debe cambiar estado de PROGRAMADA a ATENDIDA")
    void debeCambiarEstadoDeProgramadaAAtendida() {
        Cita cita = new Cita(fechaFutura, "Motivo", paciente, medico);
        
        assertDoesNotThrow(() -> {
            cita.cambiarEstado(EstadoCita.ATENDIDA);
        });
        
        assertEquals(EstadoCita.ATENDIDA, cita.getEstado());
    }

    @Test
    @DisplayName("Debe cambiar estado de PROGRAMADA a CANCELADA")
    void debeCambiarEstadoDeProgramadaACancelada() {
        Cita cita = new Cita(fechaFutura, "Motivo", paciente, medico);
        
        assertDoesNotThrow(() -> {
            cita.cambiarEstado(EstadoCita.CANCELADA);
        });
        
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
    }

    @Test
    @DisplayName("No debe cambiar estado desde ATENDIDA")
    void nDebeCambiarEstadoDesdeAtendida() {
        Cita cita = new Cita(fechaFutura, EstadoCita.ATENDIDA, "Motivo", paciente, medico);
        
        assertThrows(IllegalStateException.class, () -> {
            cita.cambiarEstado(EstadoCita.CANCELADA);
        });
    }

    @Test
    @DisplayName("No debe cambiar estado desde CANCELADA")
    void nDebeCambiarEstadoDesdeCancelada() {
        Cita cita = new Cita(fechaFutura, EstadoCita.CANCELADA, "Motivo", paciente, medico);
        
        assertThrows(IllegalStateException.class, () -> {
            cita.cambiarEstado(EstadoCita.ATENDIDA);
        });
    }

    @Test
    @DisplayName("No debe cambiar a estado nulo")
    void nDebeCambiarAEstadoNulo() {
        Cita cita = new Cita(fechaFutura, "Motivo", paciente, medico);
        
        assertThrows(IllegalArgumentException.class, () -> {
            cita.cambiarEstado(null);
        });
    }

    @Test
    @DisplayName("Debe implementar equals correctamente")
    void debeImplementarEqualsCorrectamente() {
        Cita cita1 = new Cita(fechaFutura, "Motivo 1", paciente, medico);
        Cita cita2 = new Cita(fechaFutura, "Motivo 2", paciente, medico);
        Cita cita3 = new Cita(fechaFutura.plusHours(1), "Motivo 1", paciente, medico);
        
        assertEquals(cita1, cita2); // Mismo paciente, médico y fecha
        assertNotEquals(cita1, cita3); // Diferente fecha
        assertNotEquals(cita1, null);
        assertEquals(cita1, cita1);
    }

    @Test
    @DisplayName("Debe implementar hashCode correctamente")
    void debeImplementarHashCodeCorrectamente() {
        Cita cita1 = new Cita(fechaFutura, "Motivo 1", paciente, medico);
        Cita cita2 = new Cita(fechaFutura, "Motivo 2", paciente, medico);
        
        assertEquals(cita1.hashCode(), cita2.hashCode());
    }

    @Test
    @DisplayName("Debe generar toString informativo")
    void debeGenerarToStringInformativo() {
        Cita cita = new Cita(fechaFutura, "Consulta general", paciente, medico);
        cita.setId(1L);
        
        String toString = cita.toString();
        
        assertTrue(toString.contains("Cita{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("estado=PROGRAMADA"));
        assertTrue(toString.contains("motivo='Consulta general'"));
        assertTrue(toString.contains("paciente=Juan Pérez"));
        assertTrue(toString.contains("medico=Dr. García"));
    }
}