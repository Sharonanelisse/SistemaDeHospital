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
 * Tests específicos para las relaciones ManyToOne de la entidad Cita.
 * Verifica la correcta configuración de las relaciones con Paciente y Medico.
 */
@DisplayName("Cita - Tests de Relaciones ManyToOne")
class CitaRelationshipTest {

    private Paciente paciente1;
    private Paciente paciente2;
    private Medico medico1;
    private Medico medico2;
    private LocalDateTime fechaFutura1;
    private LocalDateTime fechaFutura2;

    @BeforeEach
    void setUp() {
        // Crear pacientes de prueba
        paciente1 = new Paciente(
            "Juan Pérez",
            "1234567890123",
            LocalDate.of(1990, 1, 1),
            "12345678",
            "juan@email.com"
        );
        paciente1.setId(1L);

        paciente2 = new Paciente(
            "María García",
            "9876543210987",
            LocalDate.of(1985, 5, 15),
            "87654321",
            "maria@email.com"
        );
        paciente2.setId(2L);

        // Crear médicos de prueba
        medico1 = new Medico(
            "Dr. García",
            "COL123",
            Especialidad.CARDIOLOGIA,
            "garcia@hospital.com"
        );
        medico1.setId(1L);

        medico2 = new Medico(
            "Dra. López",
            "COL456",
            Especialidad.PEDIATRIA,
            "lopez@hospital.com"
        );
        medico2.setId(2L);

        // Fechas de prueba
        fechaFutura1 = LocalDateTime.now().plusDays(1);
        fechaFutura2 = LocalDateTime.now().plusDays(2);
    }

    @Test
    @DisplayName("Debe establecer relación ManyToOne con Paciente")
    void debeEstablecerRelacionManyToOneConPaciente() {
        Cita cita = new Cita(fechaFutura1, "Consulta general", paciente1, medico1);
        
        assertNotNull(cita.getPaciente());
        assertEquals(paciente1, cita.getPaciente());
        assertEquals("Juan Pérez", cita.getPaciente().getNombre());
        assertEquals("1234567890123", cita.getPaciente().getDpi());
    }

    @Test
    @DisplayName("Debe establecer relación ManyToOne con Medico")
    void debeEstablecerRelacionManyToOneConMedico() {
        Cita cita = new Cita(fechaFutura1, "Consulta cardiológica", paciente1, medico1);
        
        assertNotNull(cita.getMedico());
        assertEquals(medico1, cita.getMedico());
        assertEquals("Dr. García", cita.getMedico().getNombre());
        assertEquals("COL123", cita.getMedico().getColegiado());
        assertEquals(Especialidad.CARDIOLOGIA, cita.getMedico().getEspecialidad());
    }

    @Test
    @DisplayName("Debe permitir múltiples citas para el mismo paciente")
    void debePermitirMultiplesCitasParaElMismoPaciente() {
        Cita cita1 = new Cita(fechaFutura1, "Primera consulta", paciente1, medico1);
        Cita cita2 = new Cita(fechaFutura2, "Segunda consulta", paciente1, medico2);
        
        assertEquals(paciente1, cita1.getPaciente());
        assertEquals(paciente1, cita2.getPaciente());
        assertNotEquals(cita1.getMedico(), cita2.getMedico());
        assertNotEquals(cita1.getFechaHora(), cita2.getFechaHora());
    }

    @Test
    @DisplayName("Debe permitir múltiples citas para el mismo médico")
    void debePermitirMultiplesCitasParaElMismoMedico() {
        Cita cita1 = new Cita(fechaFutura1, "Consulta paciente 1", paciente1, medico1);
        Cita cita2 = new Cita(fechaFutura2, "Consulta paciente 2", paciente2, medico1);
        
        assertEquals(medico1, cita1.getMedico());
        assertEquals(medico1, cita2.getMedico());
        assertNotEquals(cita1.getPaciente(), cita2.getPaciente());
        assertNotEquals(cita1.getFechaHora(), cita2.getFechaHora());
    }

    @Test
    @DisplayName("Debe cambiar paciente de una cita existente")
    void debeCambiarPacienteDeUnaCitaExistente() {
        Cita cita = new Cita(fechaFutura1, "Consulta", paciente1, medico1);
        
        assertEquals(paciente1, cita.getPaciente());
        
        cita.setPaciente(paciente2);
        
        assertEquals(paciente2, cita.getPaciente());
        assertEquals("María García", cita.getPaciente().getNombre());
    }

    @Test
    @DisplayName("Debe cambiar médico de una cita existente")
    void debeCambiarMedicoDeUnaCitaExistente() {
        Cita cita = new Cita(fechaFutura1, "Consulta", paciente1, medico1);
        
        assertEquals(medico1, cita.getMedico());
        assertEquals(Especialidad.CARDIOLOGIA, cita.getMedico().getEspecialidad());
        
        cita.setMedico(medico2);
        
        assertEquals(medico2, cita.getMedico());
        assertEquals(Especialidad.PEDIATRIA, cita.getMedico().getEspecialidad());
    }

    @Test
    @DisplayName("Debe mantener integridad referencial al cambiar relaciones")
    void debeMantenerIntegridadReferencialAlCambiarRelaciones() {
        Cita cita = new Cita(fechaFutura1, "Consulta", paciente1, medico1);
        
        // Verificar estado inicial
        assertEquals(paciente1, cita.getPaciente());
        assertEquals(medico1, cita.getMedico());
        
        // Cambiar ambas relaciones
        cita.setPaciente(paciente2);
        cita.setMedico(medico2);
        
        // Verificar que los cambios se aplicaron correctamente
        assertEquals(paciente2, cita.getPaciente());
        assertEquals(medico2, cita.getMedico());
        
        // Verificar que las referencias anteriores no se mantienen
        assertNotEquals(paciente1, cita.getPaciente());
        assertNotEquals(medico1, cita.getMedico());
    }

    @Test
    @DisplayName("Debe crear citas con diferentes combinaciones de paciente-médico")
    void debeCrearCitasConDiferentesCombinacionesPacienteMedico() {
        Cita cita1 = new Cita(fechaFutura1, "Consulta 1", paciente1, medico1);
        Cita cita2 = new Cita(fechaFutura1.plusHours(1), "Consulta 2", paciente1, medico2);
        Cita cita3 = new Cita(fechaFutura1.plusHours(2), "Consulta 3", paciente2, medico1);
        Cita cita4 = new Cita(fechaFutura1.plusHours(3), "Consulta 4", paciente2, medico2);
        
        // Verificar combinación 1: paciente1 + medico1
        assertEquals(paciente1, cita1.getPaciente());
        assertEquals(medico1, cita1.getMedico());
        
        // Verificar combinación 2: paciente1 + medico2
        assertEquals(paciente1, cita2.getPaciente());
        assertEquals(medico2, cita2.getMedico());
        
        // Verificar combinación 3: paciente2 + medico1
        assertEquals(paciente2, cita3.getPaciente());
        assertEquals(medico1, cita3.getMedico());
        
        // Verificar combinación 4: paciente2 + medico2
        assertEquals(paciente2, cita4.getPaciente());
        assertEquals(medico2, cita4.getMedico());
    }

    @Test
    @DisplayName("Debe detectar conflictos de horario correctamente con relaciones")
    void debeDetectarConflictosDeHorarioCorrectamenteConRelaciones() {
        LocalDateTime mismaFecha = fechaFutura1;
        
        // Mismo médico, misma fecha -> conflicto
        Cita cita1 = new Cita(mismaFecha, "Consulta 1", paciente1, medico1);
        Cita cita2 = new Cita(mismaFecha, "Consulta 2", paciente2, medico1);
        
        assertTrue(cita1.conflictaConHorario(cita2));
        
        // Diferente médico, misma fecha -> no conflicto
        Cita cita3 = new Cita(mismaFecha, "Consulta 3", paciente1, medico2);
        
        assertFalse(cita1.conflictaConHorario(cita3));
        
        // Mismo médico, diferente fecha -> no conflicto
        Cita cita4 = new Cita(fechaFutura2, "Consulta 4", paciente1, medico1);
        
        assertFalse(cita1.conflictaConHorario(cita4));
    }

    @Test
    @DisplayName("Debe manejar relaciones nulas en conflictos de horario")
    void debeManejarRelacionesNulasEnConflictosDeHorario() {
        Cita citaCompleta = new Cita(fechaFutura1, "Consulta", paciente1, medico1);
        
        Cita citaConMedicoNulo = new Cita();
        citaConMedicoNulo.setFechaHora(fechaFutura1);
        citaConMedicoNulo.setPaciente(paciente1);
        // medico queda null
        
        Cita citaConFechaNula = new Cita();
        citaConFechaNula.setPaciente(paciente1);
        citaConFechaNula.setMedico(medico1);
        // fechaHora queda null
        
        // No debe haber conflicto cuando hay valores nulos
        assertFalse(citaCompleta.conflictaConHorario(citaConMedicoNulo));
        assertFalse(citaCompleta.conflictaConHorario(citaConFechaNula));
        assertFalse(citaCompleta.conflictaConHorario(null));
    }

    @Test
    @DisplayName("Debe mantener consistencia en equals con relaciones")
    void debeMantenerConsistenciaEnEqualsConRelaciones() {
        LocalDateTime mismaFecha = fechaFutura1;
        
        Cita cita1 = new Cita(mismaFecha, "Motivo 1", paciente1, medico1);
        Cita cita2 = new Cita(mismaFecha, "Motivo 2", paciente1, medico1);
        Cita cita3 = new Cita(mismaFecha, "Motivo 1", paciente2, medico1);
        Cita cita4 = new Cita(mismaFecha, "Motivo 1", paciente1, medico2);
        
        // Misma fecha, mismo paciente, mismo médico -> iguales
        assertEquals(cita1, cita2);
        
        // Diferente paciente -> no iguales
        assertNotEquals(cita1, cita3);
        
        // Diferente médico -> no iguales
        assertNotEquals(cita1, cita4);
    }

    @Test
    @DisplayName("Debe incluir información de relaciones en toString")
    void debeIncluirInformacionDeRelacionesEnToString() {
        Cita cita = new Cita(fechaFutura1, "Consulta cardiológica", paciente1, medico1);
        cita.setId(1L);
        
        String toString = cita.toString();
        
        assertTrue(toString.contains("paciente=Juan Pérez"));
        assertTrue(toString.contains("medico=Dr. García"));
        
        // Probar con relaciones nulas
        Cita citaSinRelaciones = new Cita();
        citaSinRelaciones.setId(2L);
        citaSinRelaciones.setFechaHora(fechaFutura1);
        citaSinRelaciones.setEstado(EstadoCita.PROGRAMADA);
        
        String toStringNulo = citaSinRelaciones.toString();
        
        assertTrue(toStringNulo.contains("paciente=null"));
        assertTrue(toStringNulo.contains("medico=null"));
    }
}