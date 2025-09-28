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
 * Tests para verificar la funcionalidad del menú de consultas
 * Requerimientos: 7.1, 7.2, 5.1, 5.2, 5.3, 5.4, 5.5
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConsultationMenuTest {
    
    private static EntityManagerFactory emf;
    private static PacienteService pacienteService;
    private static MedicoService medicoService;
    private static CitaService citaService;
    private static HistorialMedicoService historialMedicoService;
    
    private static Paciente pacientePrueba;
    private static Medico medicoPrueba;
    private static Cita citaPrueba;
    private static HistorialMedico historialPrueba;
    
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
    @DisplayName("Crear datos de prueba para consultas")
    void crearDatosPrueba() {
        // Crear paciente de prueba
        pacientePrueba = pacienteService.registrarPaciente(
            "Juan Pérez Consulta",
            "1234567890123",
            LocalDate.of(1990, 5, 15),
            "555-0123",
            "juan.consulta@test.com"
        );
        
        assertNotNull(pacientePrueba);
        assertNotNull(pacientePrueba.getId());
        
        // Crear médico de prueba
        medicoPrueba = medicoService.registrarMedico(
            "Dr. María González Consulta",
            "COL-98765",
            Especialidad.CARDIOLOGIA,
            "maria.consulta@hospital.com"
        );
        
        assertNotNull(medicoPrueba);
        assertNotNull(medicoPrueba.getId());
        
        // Crear historial médico
        historialPrueba = historialMedicoService.crearHistorial(
            pacientePrueba.getId(),
            "Alergia a penicilina",
            "Hipertensión familiar",
            "Paciente colaborador"
        );
        
        assertNotNull(historialPrueba);
        
        // Crear cita de prueba
        citaPrueba = citaService.agendarCita(
            pacientePrueba.getId(),
            medicoPrueba.getId(),
            LocalDateTime.now().plusDays(7),
            "Consulta de seguimiento"
        );
        
        assertNotNull(citaPrueba);
        assertNotNull(citaPrueba.getId());
    }
    
    @Test
    @Order(2)
    @DisplayName("Verificar listado de pacientes con citas")
    void verificarListadoPacientesConCitas() {
        // Obtener lista de pacientes
        List<Paciente> pacientes = pacienteService.listarPacientes();
        
        assertFalse(pacientes.isEmpty(), "Debe haber al menos un paciente");
        
        // Verificar que nuestro paciente de prueba está en la lista
        boolean pacienteEncontrado = pacientes.stream()
            .anyMatch(p -> p.getId().equals(pacientePrueba.getId()));
        
        assertTrue(pacienteEncontrado, "El paciente de prueba debe estar en la lista");
        
        // Verificar que se pueden obtener las citas del paciente
        List<Cita> citasPaciente = citaService.listarCitasPorPaciente(pacientePrueba.getId());
        assertFalse(citasPaciente.isEmpty(), "El paciente debe tener al menos una cita");
        
        // Verificar que la cita tiene la información correcta
        Cita cita = citasPaciente.get(0);
        assertEquals(pacientePrueba.getId(), cita.getPaciente().getId());
        assertEquals(medicoPrueba.getId(), cita.getMedico().getId());
        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
    }
    
    @Test
    @Order(3)
    @DisplayName("Verificar listado de médicos con próximas citas")
    void verificarListadoMedicosConProximasCitas() {
        // Obtener lista de médicos con próximas citas
        List<Medico> medicos = medicoService.listarMedicosConProximasCitas();
        
        assertFalse(medicos.isEmpty(), "Debe haber al menos un médico");
        
        // Verificar que nuestro médico de prueba está en la lista
        boolean medicoEncontrado = medicos.stream()
            .anyMatch(m -> m.getId().equals(medicoPrueba.getId()));
        
        assertTrue(medicoEncontrado, "El médico de prueba debe estar en la lista");
        
        // Verificar que se pueden obtener las próximas citas del médico
        List<Cita> proximasCitas = citaService.listarProximasCitasPorMedico(medicoPrueba.getId());
        assertFalse(proximasCitas.isEmpty(), "El médico debe tener al menos una próxima cita");
        
        // Verificar que las citas están ordenadas por fecha
        for (int i = 1; i < proximasCitas.size(); i++) {
            LocalDateTime fechaAnterior = proximasCitas.get(i - 1).getFechaHora();
            LocalDateTime fechaActual = proximasCitas.get(i).getFechaHora();
            assertTrue(fechaAnterior.isBefore(fechaActual) || fechaAnterior.isEqual(fechaActual),
                "Las citas deben estar ordenadas por fecha");
        }
    }
    
    @Test
    @Order(4)
    @DisplayName("Verificar búsqueda de citas por rango de fechas")
    void verificarBusquedaCitasPorRangoFechas() {
        // Definir rango de fechas que incluya nuestra cita de prueba
        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = LocalDate.now().plusDays(30);
        
        // Buscar citas en el rango
        List<Cita> citasEnRango = citaService.buscarCitasPorRangoFechas(fechaInicio, fechaFin);
        
        assertFalse(citasEnRango.isEmpty(), "Debe haber al menos una cita en el rango");
        
        // Verificar que nuestra cita de prueba está en el resultado
        boolean citaEncontrada = citasEnRango.stream()
            .anyMatch(c -> c.getId().equals(citaPrueba.getId()));
        
        assertTrue(citaEncontrada, "La cita de prueba debe estar en el rango");
        
        // Verificar que todas las citas están dentro del rango
        for (Cita cita : citasEnRango) {
            LocalDate fechaCita = cita.getFechaHora().toLocalDate();
            assertTrue(
                (fechaCita.isEqual(fechaInicio) || fechaCita.isAfter(fechaInicio)) &&
                (fechaCita.isEqual(fechaFin) || fechaCita.isBefore(fechaFin)),
                "Todas las citas deben estar dentro del rango especificado"
            );
        }
    }
    
    @Test
    @Order(5)
    @DisplayName("Verificar consulta de historial médico de paciente")
    void verificarConsultaHistorialMedico() {
        // Consultar historial del paciente de prueba
        var historialOpt = historialMedicoService.consultarHistorial(pacientePrueba.getId());
        
        assertTrue(historialOpt.isPresent(), "El paciente debe tener historial médico");
        
        HistorialMedico historial = historialOpt.get();
        
        // Verificar que el historial tiene la información correcta
        assertEquals(pacientePrueba.getId(), historial.getId());
        assertEquals("Alergia a penicilina", historial.getAlergias());
        assertEquals("Hipertensión familiar", historial.getAntecedentes());
        assertEquals("Paciente colaborador", historial.getObservaciones());
        
        // Verificar que se puede acceder a la información del paciente
        assertNotNull(historial.getPaciente());
        assertEquals(pacientePrueba.getNombre(), historial.getPaciente().getNombre());
        assertEquals(pacientePrueba.getDpi(), historial.getPaciente().getDpi());
    }
    
    @Test
    @Order(6)
    @DisplayName("Verificar manejo de casos sin datos")
    void verificarManejoSinDatos() {
        // Crear un paciente sin citas ni historial
        Paciente pacienteSinDatos = pacienteService.registrarPaciente(
            "Paciente Sin Datos",
            "9999999999999",
            LocalDate.of(1985, 3, 10),
            null,
            "sindatos@test.com"
        );
        
        // Verificar que no tiene citas
        List<Cita> citasSinDatos = citaService.listarCitasPorPaciente(pacienteSinDatos.getId());
        assertTrue(citasSinDatos.isEmpty(), "El paciente sin datos no debe tener citas");
        
        // Verificar que no tiene historial
        var historialSinDatos = historialMedicoService.consultarHistorial(pacienteSinDatos.getId());
        assertTrue(historialSinDatos.isEmpty(), "El paciente sin datos no debe tener historial");
        
        // Verificar búsqueda en rango sin resultados
        LocalDate fechaPasada = LocalDate.now().minusDays(30);
        LocalDate fechaPasadaFin = LocalDate.now().minusDays(20);
        
        List<Cita> citasPasadas = citaService.buscarCitasPorRangoFechas(fechaPasada, fechaPasadaFin);
        // Puede estar vacío o no, dependiendo de otros datos de prueba
        assertNotNull(citasPasadas, "La búsqueda debe retornar una lista (puede estar vacía)");
    }
    
    @Test
    @Order(7)
    @DisplayName("Verificar estadísticas de citas por estado")
    void verificarEstadisticasCitas() {
        // Obtener todas las citas
        List<Cita> todasLasCitas = citaService.listarTodasLasCitas();
        
        assertFalse(todasLasCitas.isEmpty(), "Debe haber al menos una cita");
        
        // Contar citas por estado
        long programadas = todasLasCitas.stream()
            .filter(c -> c.getEstado() == EstadoCita.PROGRAMADA)
            .count();
        
        long atendidas = todasLasCitas.stream()
            .filter(c -> c.getEstado() == EstadoCita.ATENDIDA)
            .count();
        
        long canceladas = todasLasCitas.stream()
            .filter(c -> c.getEstado() == EstadoCita.CANCELADA)
            .count();
        
        // Verificar que la suma coincide con el total
        assertEquals(todasLasCitas.size(), programadas + atendidas + canceladas,
            "La suma de citas por estado debe coincidir con el total");
        
        // Debe haber al menos una cita programada (nuestra cita de prueba)
        assertTrue(programadas >= 1, "Debe haber al menos una cita programada");
    }
}