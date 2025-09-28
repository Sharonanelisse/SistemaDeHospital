package com.darwinruiz.hospital.services;

import com.darwinruiz.hospital.enums.Especialidad;
import com.darwinruiz.hospital.enums.EstadoCita;
import com.darwinruiz.hospital.exceptions.CitaConflictoHorarioException;
import com.darwinruiz.hospital.exceptions.FechaInvalidaException;
import com.darwinruiz.hospital.models.Cita;
import com.darwinruiz.hospital.models.Medico;
import com.darwinruiz.hospital.models.Paciente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para CitaService.
 * Verifica las validaciones de fecha futura y conflicto de horarios.
 * Requerimientos: 4.3, 4.4 - Validaciones de fecha y conflicto de horarios
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CitaServiceTest {
    
    private static EntityManagerFactory emf;
    private static CitaService citaService;
    private static PacienteService pacienteService;
    private static MedicoService medicoService;
    
    private static Paciente pacientePrueba;
    private static Medico medicoPrueba;
    
    @BeforeAll
    static void setUpClass() {
        emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
        citaService = new CitaService(emf);
        pacienteService = new PacienteService(emf);
        medicoService = new MedicoService(emf);
        
        // Limpiar datos de prueba previos
        limpiarDatosPrueba();
        
        // Crear paciente y médico de prueba
        crearDatosPrueba();
    }
    
    @AfterAll
    static void tearDownClass() {
        limpiarDatosPrueba();
        if (citaService != null) {
            citaService.close();
        }
        if (pacienteService != null) {
            pacienteService.close();
        }
        if (medicoService != null) {
            medicoService.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
    
    private static void limpiarDatosPrueba() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Cita c WHERE c.paciente.dpi LIKE 'TESTCITA%' OR c.medico.colegiado LIKE 'TESTCITA%'").executeUpdate();
            em.createQuery("DELETE FROM HistorialMedico h WHERE h.paciente.dpi LIKE 'TESTCITA%'").executeUpdate();
            em.createQuery("DELETE FROM Paciente p WHERE p.dpi LIKE 'TESTCITA%'").executeUpdate();
            em.createQuery("DELETE FROM Medico m WHERE m.colegiado LIKE 'TESTCITA%'").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
    }
    
    private static void crearDatosPrueba() {
        // Crear paciente de prueba
        pacientePrueba = pacienteService.registrarPaciente(
            "Paciente Test Cita", 
            "TESTCITAPAC001", 
            LocalDate.of(1990, 1, 1), 
            "12345678", 
            "paciente.cita@test.com"
        );
        
        // Crear médico de prueba
        medicoPrueba = medicoService.registrarMedico(
            "Dr. Test Cita", 
            "TESTCITAMED001", 
            Especialidad.CARDIOLOGIA, 
            "medico.cita@test.com"
        );
    }
    
    @Test
    @Order(1)
    @DisplayName("Debe agendar una cita con datos válidos")
    void testAgendarCitaValida() {
        // Arrange
        LocalDateTime fechaHoraFutura = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        String motivo = "Consulta de rutina";
        
        // Act
        Cita cita = citaService.agendarCita(pacientePrueba.getId(), medicoPrueba.getId(), fechaHoraFutura, motivo);
        
        // Assert
        assertNotNull(cita);
        assertNotNull(cita.getId());
        assertEquals(fechaHoraFutura, cita.getFechaHora());
        assertEquals(motivo, cita.getMotivo());
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
        assertEquals(pacientePrueba.getId(), cita.getPaciente().getId());
        assertEquals(medicoPrueba.getId(), cita.getMedico().getId());
    }
    
    @Test
    @Order(2)
    @DisplayName("Debe lanzar excepción con fecha en el pasado")
    void testAgendarCitaFechaPasado() {
        // Arrange
        LocalDateTime fechaPasado = LocalDateTime.now().minusDays(1);
        
        // Act & Assert
        FechaInvalidaException exception = assertThrows(
            FechaInvalidaException.class,
            () -> citaService.agendarCita(pacientePrueba.getId(), medicoPrueba.getId(), fechaPasado, "Motivo")
        );
        
        assertTrue(exception.getMessage().contains("no es válida"));
    }
    
    @Test
    @Order(3)
    @DisplayName("Debe lanzar excepción por conflicto de horarios")
    void testAgendarCitaConflictoHorario() {
        // Arrange - Agendar una cita
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0);
        citaService.agendarCita(pacientePrueba.getId(), medicoPrueba.getId(), fechaHora, "Primera cita");
        
        // Act & Assert - Intentar agendar otra cita en el mismo horario con el mismo médico
        CitaConflictoHorarioException exception = assertThrows(
            CitaConflictoHorarioException.class,
            () -> citaService.agendarCita(pacientePrueba.getId(), medicoPrueba.getId(), fechaHora, "Segunda cita")
        );
        
        assertTrue(exception.getMessage().contains("ya tiene una cita programada"));
    }
    
    @Test
    @Order(4)
    @DisplayName("Debe lanzar excepción con datos nulos")
    void testAgendarCitaDatosNulos() {
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(1);
        
        // Paciente ID nulo
        assertThrows(IllegalArgumentException.class, 
            () -> citaService.agendarCita(null, medicoPrueba.getId(), fechaFutura, "Motivo"));
        
        // Médico ID nulo
        assertThrows(IllegalArgumentException.class, 
            () -> citaService.agendarCita(pacientePrueba.getId(), null, fechaFutura, "Motivo"));
        
        // Fecha nula
        assertThrows(IllegalArgumentException.class, 
            () -> citaService.agendarCita(pacientePrueba.getId(), medicoPrueba.getId(), null, "Motivo"));
    }
    
    @Test
    @Order(5)
    @DisplayName("Debe lanzar excepción con paciente inexistente")
    void testAgendarCitaPacienteInexistente() {
        // Arrange
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(1);
        
        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> citaService.agendarCita(99999L, medicoPrueba.getId(), fechaFutura, "Motivo")
        );
        
        assertTrue(exception.getMessage().contains("No se encontró el paciente"));
    }
    
    @Test
    @Order(6)
    @DisplayName("Debe lanzar excepción con médico inexistente")
    void testAgendarCitaMedicoInexistente() {
        // Arrange
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(1);
        
        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> citaService.agendarCita(pacientePrueba.getId(), 99999L, fechaFutura, "Motivo")
        );
        
        assertTrue(exception.getMessage().contains("No se encontró el médico"));
    }
    
    @Test
    @Order(7)
    @DisplayName("Debe cambiar estado de cita de PROGRAMADA a ATENDIDA")
    void testCambiarEstadoCitaAtendida() {
        // Arrange - Crear una cita
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(3).withHour(9).withMinute(0).withSecond(0).withNano(0);
        Cita cita = citaService.agendarCita(pacientePrueba.getId(), medicoPrueba.getId(), fechaHora, "Para atender");
        
        // Act
        Cita citaActualizada = citaService.cambiarEstadoCita(cita.getId(), EstadoCita.ATENDIDA);
        
        // Assert
        assertNotNull(citaActualizada);
        assertEquals(EstadoCita.ATENDIDA, citaActualizada.getEstado());
    }
    
    @Test
    @Order(8)
    @DisplayName("Debe cambiar estado de cita de PROGRAMADA a CANCELADA")
    void testCambiarEstadoCitaCancelada() {
        // Arrange - Crear una cita
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(4).withHour(11).withMinute(0).withSecond(0).withNano(0);
        Cita cita = citaService.agendarCita(pacientePrueba.getId(), medicoPrueba.getId(), fechaHora, "Para cancelar");
        
        // Act
        Cita citaActualizada = citaService.cambiarEstadoCita(cita.getId(), EstadoCita.CANCELADA);
        
        // Assert
        assertNotNull(citaActualizada);
        assertEquals(EstadoCita.CANCELADA, citaActualizada.getEstado());
    }
    
    @Test
    @Order(9)
    @DisplayName("Debe lanzar excepción al cambiar estado de cita ATENDIDA")
    void testCambiarEstadoCitaAtendidaInvalido() {
        // Arrange - Crear y atender una cita
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(5).withHour(15).withMinute(0).withSecond(0).withNano(0);
        Cita cita = citaService.agendarCita(pacientePrueba.getId(), medicoPrueba.getId(), fechaHora, "Ya atendida");
        citaService.cambiarEstadoCita(cita.getId(), EstadoCita.ATENDIDA);
        
        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> citaService.cambiarEstadoCita(cita.getId(), EstadoCita.CANCELADA)
        );
        
        assertTrue(exception.getMessage().contains("ATENDIDA no puede cambiar"));
    }
    
    @Test
    @Order(10)
    @DisplayName("Debe listar citas por paciente")
    void testListarCitasPorPaciente() {
        // Arrange - Crear varias citas para el paciente
        LocalDateTime fecha1 = LocalDateTime.now().plusDays(6).withHour(8).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fecha2 = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0).withSecond(0).withNano(0);
        
        citaService.agendarCita(pacientePrueba.getId(), medicoPrueba.getId(), fecha1, "Cita 1");
        citaService.agendarCita(pacientePrueba.getId(), medicoPrueba.getId(), fecha2, "Cita 2");
        
        // Act
        List<Cita> citas = citaService.listarCitasPorPaciente(pacientePrueba.getId());
        
        // Assert
        assertNotNull(citas);
        assertFalse(citas.isEmpty());
        
        // Verificar que todas las citas pertenecen al paciente
        boolean todasDelPaciente = citas.stream()
            .allMatch(c -> c.getPaciente().getId().equals(pacientePrueba.getId()));
        assertTrue(todasDelPaciente);
    }
    
    @Test
    @Order(11)
    @DisplayName("Debe listar próximas citas por médico")
    void testListarProximasCitasPorMedico() {
        // Act
        List<Cita> proximasCitas = citaService.listarProximasCitasPorMedico(medicoPrueba.getId());
        
        // Assert
        assertNotNull(proximasCitas);
        assertFalse(proximasCitas.isEmpty());
        
        // Verificar que todas las citas pertenecen al médico y son futuras
        LocalDateTime ahora = LocalDateTime.now();
        boolean todasDelMedicoYFuturas = proximasCitas.stream()
            .allMatch(c -> c.getMedico().getId().equals(medicoPrueba.getId()) && 
                          c.getFechaHora().isAfter(ahora));
        assertTrue(todasDelMedicoYFuturas);
    }
    
    @Test
    @Order(12)
    @DisplayName("Debe buscar citas por rango de fechas")
    void testBuscarCitasPorRangoFechas() {
        // Arrange
        LocalDate fechaInicio = LocalDate.now().plusDays(1);
        LocalDate fechaFin = LocalDate.now().plusDays(10);
        
        // Act
        List<Cita> citasEnRango = citaService.buscarCitasPorRangoFechas(fechaInicio, fechaFin);
        
        // Assert
        assertNotNull(citasEnRango);
        
        // Verificar que todas las citas están en el rango
        boolean todasEnRango = citasEnRango.stream()
            .allMatch(c -> {
                LocalDate fechaCita = c.getFechaHora().toLocalDate();
                return !fechaCita.isBefore(fechaInicio) && !fechaCita.isAfter(fechaFin);
            });
        assertTrue(todasEnRango);
    }
    
    @Test
    @Order(13)
    @DisplayName("Debe lanzar excepción con rango de fechas inválido")
    void testBuscarCitasRangoFechasInvalido() {
        // Arrange
        LocalDate fechaInicio = LocalDate.now().plusDays(10);
        LocalDate fechaFin = LocalDate.now().plusDays(1); // Fecha fin anterior a inicio
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> citaService.buscarCitasPorRangoFechas(fechaInicio, fechaFin));
    }
    
    @Test
    @Order(14)
    @DisplayName("Debe eliminar cita existente")
    void testEliminarCita() {
        // Arrange - Crear una cita
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(8).withHour(16).withMinute(0).withSecond(0).withNano(0);
        Cita cita = citaService.agendarCita(pacientePrueba.getId(), medicoPrueba.getId(), fechaHora, "Para eliminar");
        
        // Act
        boolean eliminada = citaService.eliminarCita(cita.getId());
        
        // Assert
        assertTrue(eliminada);
        
        // Verificar que ya no existe
        Optional<Cita> citaBuscada = citaService.buscarPorId(cita.getId());
        assertFalse(citaBuscada.isPresent());
    }
    
    @Test
    @Order(15)
    @DisplayName("Debe retornar false al eliminar cita inexistente")
    void testEliminarCitaInexistente() {
        // Act
        boolean eliminada = citaService.eliminarCita(99999L);
        
        // Assert
        assertFalse(eliminada);
    }
    
    @Test
    @Order(16)
    @DisplayName("Debe actualizar cita programada")
    void testActualizarCita() {
        // Arrange - Crear una cita
        LocalDateTime fechaOriginal = LocalDateTime.now().plusDays(9).withHour(12).withMinute(0).withSecond(0).withNano(0);
        Cita cita = citaService.agendarCita(pacientePrueba.getId(), medicoPrueba.getId(), fechaOriginal, "Motivo original");
        
        // Act - Actualizar fecha y motivo
        LocalDateTime nuevaFecha = LocalDateTime.now().plusDays(10).withHour(14).withMinute(0).withSecond(0).withNano(0);
        String nuevoMotivo = "Motivo actualizado";
        Cita citaActualizada = citaService.actualizarCita(cita.getId(), nuevaFecha, nuevoMotivo);
        
        // Assert
        assertNotNull(citaActualizada);
        assertEquals(nuevaFecha, citaActualizada.getFechaHora());
        assertEquals(nuevoMotivo, citaActualizada.getMotivo());
        assertEquals(EstadoCita.PROGRAMADA, citaActualizada.getEstado());
    }
    
    @Test
    @Order(17)
    @DisplayName("Debe lanzar excepción al actualizar cita con conflicto de horario")
    void testActualizarCitaConflictoHorario() {
        // Arrange - Crear dos citas
        LocalDateTime fecha1 = LocalDateTime.now().plusDays(11).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fecha2 = LocalDateTime.now().plusDays(11).withHour(11).withMinute(0).withSecond(0).withNano(0);
        
        Cita cita1 = citaService.agendarCita(pacientePrueba.getId(), medicoPrueba.getId(), fecha1, "Cita 1");
        Cita cita2 = citaService.agendarCita(pacientePrueba.getId(), medicoPrueba.getId(), fecha2, "Cita 2");
        
        // Act & Assert - Intentar actualizar cita2 a la misma hora que cita1
        assertThrows(CitaConflictoHorarioException.class, 
            () -> citaService.actualizarCita(cita2.getId(), fecha1, "Conflicto"));
    }
    
    @Test
    @Order(18)
    @DisplayName("Debe listar citas por estado")
    void testListarCitasPorEstado() {
        // Act
        List<Cita> citasProgramadas = citaService.listarCitasPorEstado(EstadoCita.PROGRAMADA);
        List<Cita> citasAtendidas = citaService.listarCitasPorEstado(EstadoCita.ATENDIDA);
        List<Cita> citasCanceladas = citaService.listarCitasPorEstado(EstadoCita.CANCELADA);
        
        // Assert
        assertNotNull(citasProgramadas);
        assertNotNull(citasAtendidas);
        assertNotNull(citasCanceladas);
        
        // Verificar que todas las citas tienen el estado correcto
        boolean todasProgramadas = citasProgramadas.stream()
            .allMatch(c -> c.getEstado() == EstadoCita.PROGRAMADA);
        assertTrue(todasProgramadas);
        
        boolean todasAtendidas = citasAtendidas.stream()
            .allMatch(c -> c.getEstado() == EstadoCita.ATENDIDA);
        assertTrue(todasAtendidas);
        
        boolean todasCanceladas = citasCanceladas.stream()
            .allMatch(c -> c.getEstado() == EstadoCita.CANCELADA);
        assertTrue(todasCanceladas);
    }
}