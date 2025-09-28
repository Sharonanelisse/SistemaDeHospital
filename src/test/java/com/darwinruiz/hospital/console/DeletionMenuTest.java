package com.darwinruiz.hospital.console;

import com.darwinruiz.hospital.enums.Especialidad;
import com.darwinruiz.hospital.enums.EstadoCita;
import com.darwinruiz.hospital.models.Cita;
import com.darwinruiz.hospital.models.HistorialMedico;
import com.darwinruiz.hospital.models.Medico;
import com.darwinruiz.hospital.models.Paciente;
import com.darwinruiz.hospital.services.CitaService;
import com.darwinruiz.hospital.services.HistorialMedicoService;
import com.darwinruiz.hospital.services.MedicoService;
import com.darwinruiz.hospital.services.PacienteService;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para verificar la funcionalidad del menú de eliminación
 * Requerimientos: 6.1, 6.2, 6.3 - Eliminación de citas y pacientes
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeletionMenuTest {
    
    private static EntityManagerFactory emf;
    private static PacienteService pacienteService;
    private static MedicoService medicoService;
    private static CitaService citaService;
    private static HistorialMedicoService historialMedicoService;
    
    private static Paciente pacientePrueba;
    private static Medico medicoPrueba;
    private static Cita citaPrueba;
    
    @BeforeAll
    static void setUp() {
        emf = Persistence.createEntityManagerFactory("HospitalSystemPU");
        pacienteService = new PacienteService(emf);
        medicoService = new MedicoService(emf);
        citaService = new CitaService(emf);
        historialMedicoService = new HistorialMedicoService(emf);
    }
    
    @AfterAll
    static void tearDown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("Preparar datos de prueba para eliminación")
    void prepararDatosPrueba() {
        // Crear paciente de prueba
        pacientePrueba = pacienteService.registrarPaciente(
            "Paciente Para Eliminar",
            "1234567890123",
            LocalDate.of(1990, 1, 1),
            "12345678",
            "eliminar@test.com"
        );
        
        assertNotNull(pacientePrueba);
        assertNotNull(pacientePrueba.getId());
        
        // Crear médico de prueba
        medicoPrueba = medicoService.registrarMedico(
            "Dr. Eliminar Test",
            "COL-ELIM-001",
            Especialidad.CARDIOLOGIA,
            "dr.eliminar@test.com"
        );
        
        assertNotNull(medicoPrueba);
        assertNotNull(medicoPrueba.getId());
        
        // Crear historial médico
        HistorialMedico historial = historialMedicoService.crearOActualizarHistorial(
            pacientePrueba.getId(),
            "Alergia a pruebas",
            "Antecedentes de testing",
            "Observaciones de eliminación"
        );
        
        assertNotNull(historial);
        
        // Crear cita de prueba
        citaPrueba = citaService.agendarCita(
            pacientePrueba.getId(),
            medicoPrueba.getId(),
            LocalDateTime.now().plusDays(1),
            "Cita para eliminar"
        );
        
        assertNotNull(citaPrueba);
        assertNotNull(citaPrueba.getId());
    }
    
    @Test
    @Order(2)
    @DisplayName("Verificar que existen datos antes de eliminar")
    void verificarDatosExisten() {
        // Verificar que el paciente existe
        List<Paciente> pacientes = pacienteService.listarPacientes();
        assertTrue(pacientes.stream().anyMatch(p -> p.getId().equals(pacientePrueba.getId())));
        
        // Verificar que el médico existe
        List<Medico> medicos = medicoService.listarMedicosConProximasCitas();
        assertTrue(medicos.stream().anyMatch(m -> m.getId().equals(medicoPrueba.getId())));
        
        // Verificar que la cita existe
        List<Cita> citas = citaService.listarCitasPorPaciente(pacientePrueba.getId());
        assertFalse(citas.isEmpty());
        assertTrue(citas.stream().anyMatch(c -> c.getId().equals(citaPrueba.getId())));
        
        // Verificar que el historial existe
        assertTrue(historialMedicoService.existeHistorial(pacientePrueba.getId()));
    }
    
    @Test
    @Order(3)
    @DisplayName("Eliminar cita - Requerimiento 6.1")
    void eliminarCita() {
        // Verificar que la cita existe antes de eliminar
        List<Cita> citasAntes = citaService.listarTodasLasCitas();
        assertTrue(citasAntes.stream().anyMatch(c -> c.getId().equals(citaPrueba.getId())));
        
        // Eliminar la cita
        boolean eliminada = citaService.eliminarCita(citaPrueba.getId());
        assertTrue(eliminada, "La cita debería haberse eliminado exitosamente");
        
        // Verificar que la cita ya no existe
        List<Cita> citasDespues = citaService.listarTodasLasCitas();
        assertFalse(citasDespues.stream().anyMatch(c -> c.getId().equals(citaPrueba.getId())));
        
        // Verificar que el paciente y médico siguen existiendo
        List<Paciente> pacientes = pacienteService.listarPacientes();
        assertTrue(pacientes.stream().anyMatch(p -> p.getId().equals(pacientePrueba.getId())));
        
        List<Medico> medicos = medicoService.listarMedicosConProximasCitas();
        assertTrue(medicos.stream().anyMatch(m -> m.getId().equals(medicoPrueba.getId())));
    }
    
    @Test
    @Order(4)
    @DisplayName("Eliminar paciente con cascadas - Requerimientos 6.2, 6.3")
    void eliminarPacienteConCascadas() {
        // Crear nueva cita para probar cascadas
        Cita nuevaCita = citaService.agendarCita(
            pacientePrueba.getId(),
            medicoPrueba.getId(),
            LocalDateTime.now().plusDays(2),
            "Nueva cita para cascada"
        );
        
        assertNotNull(nuevaCita);
        
        // Verificar que existen datos relacionados antes de eliminar
        assertTrue(historialMedicoService.existeHistorial(pacientePrueba.getId()));
        List<Cita> citasPaciente = citaService.listarCitasPorPaciente(pacientePrueba.getId());
        assertFalse(citasPaciente.isEmpty());
        
        // Eliminar el paciente
        boolean eliminado = pacienteService.eliminarPaciente(pacientePrueba.getId());
        assertTrue(eliminado, "El paciente debería haberse eliminado exitosamente");
        
        // Verificar que el paciente ya no existe
        List<Paciente> pacientes = pacienteService.listarPacientes();
        assertFalse(pacientes.stream().anyMatch(p -> p.getId().equals(pacientePrueba.getId())));
        
        // Verificar que el historial médico fue eliminado en cascada
        assertFalse(historialMedicoService.existeHistorial(pacientePrueba.getId()));
        
        // Verificar que las citas fueron eliminadas en cascada
        List<Cita> citasDespues = citaService.listarCitasPorPaciente(pacientePrueba.getId());
        assertTrue(citasDespues.isEmpty());
        
        // Verificar que el médico NO fue eliminado
        List<Medico> medicos = medicoService.listarMedicosConProximasCitas();
        assertTrue(medicos.stream().anyMatch(m -> m.getId().equals(medicoPrueba.getId())));
    }
    
    @Test
    @Order(5)
    @DisplayName("Verificar integridad después de eliminaciones")
    void verificarIntegridadSistema() {
        // El sistema debe seguir funcionando correctamente después de las eliminaciones
        
        // Crear nuevo paciente para verificar que el sistema funciona
        Paciente nuevoPaciente = pacienteService.registrarPaciente(
            "Paciente Post Eliminación",
            "9876543210987",
            LocalDate.of(1985, 5, 15),
            "87654321",
            "post.eliminacion@test.com"
        );
        
        assertNotNull(nuevoPaciente);
        
        // Crear nueva cita con el médico existente
        Cita nuevaCita = citaService.agendarCita(
            nuevoPaciente.getId(),
            medicoPrueba.getId(),
            LocalDateTime.now().plusDays(3),
            "Cita post eliminación"
        );
        
        assertNotNull(nuevaCita);
        
        // Limpiar datos de prueba
        citaService.eliminarCita(nuevaCita.getId());
        pacienteService.eliminarPaciente(nuevoPaciente.getId());
    }
    
    @Test
    @Order(6)
    @DisplayName("Limpiar datos de prueba restantes")
    void limpiarDatosPrueba() {
        // Eliminar médico de prueba si aún existe
        try {
            medicoService.eliminarMedico(medicoPrueba.getId());
        } catch (Exception e) {
            // Puede fallar si tiene citas, es esperado
            System.out.println("Médico no eliminado (puede tener citas): " + e.getMessage());
        }
    }
}